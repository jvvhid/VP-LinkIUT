class VoiceChat {
    constructor(sessionId, currentUserId, stompClient) {
        this.sessionId = sessionId;
        this.currentUserId = currentUserId;
        this.stompClient = stompClient;
        this.peerConnections = {}; // Map of userId -> RTCPeerConnection
        this.localStream = null;
        
        // Audio elements for playing remote streams
        this.audioContainer = document.createElement('div');
        this.audioContainer.id = 'voice-chat-audio-container';
        this.audioContainer.style.display = 'none';
        document.body.appendChild(this.audioContainer);

        // STUN server configuration for NAT traversal
        this.iceServers = {
            iceServers: [
                { urls: 'stun:stun.l.google.com:19302' },
                { urls: 'stun:stun1.l.google.com:19302' }
            ]
        };

        this.subscribeToSignaling();
    }

    subscribeToSignaling() {
        this.stompClient.subscribe('/topic/voice/' + this.sessionId, (message) => {
            const signal = JSON.parse(message.body);
            
            // Ignore our own signals
            if (signal.from === this.currentUserId) return;

            console.log('Received signal:', signal.type, 'from:', signal.from);

            if (signal.type === 'peer_joined') {
                // Another peer joined, initiate a connection if we are already in call
                if (this.localStream) {
                    this.createPeerConnection(signal.from, true);
                }
            } else if (signal.type === 'offer') {
                this.handleOffer(signal);
            } else if (signal.type === 'answer') {
                this.handleAnswer(signal);
            } else if (signal.type === 'ice') {
                this.handleIceCandidate(signal);
            } else if (signal.type === 'peer_left') {
                this.handlePeerLeft(signal.from);
            }
        });
    }

    async startCall() {
        try {
            this.localStream = await navigator.mediaDevices.getUserMedia({ audio: true, video: false });
            
            // Notify others in the session that we joined
            this.sendSignal({ type: 'peer_joined' });
            console.log('Voice call started');
        } catch (e) {
            console.error('Error accessing microphone', e);
            alert('Could not access microphone for voice chat.');
            
            // Reset UI
            document.getElementById('call-controls').classList.add('hidden');
            document.getElementById('btn-start-call').classList.remove('hidden');
        }
    }

    endCall() {
        if (this.localStream) {
            this.localStream.getTracks().forEach(track => track.stop());
            this.localStream = null;
        }

        // Close all peer connections
        for (let userId in this.peerConnections) {
            this.peerConnections[userId].close();
            delete this.peerConnections[userId];
        }

        // Remove all audio elements
        this.audioContainer.innerHTML = '';

        // Notify others
        this.sendSignal({ type: 'peer_left' });
        console.log('Voice call ended');
    }

    toggleMute() {
        if (this.localStream) {
            const audioTrack = this.localStream.getAudioTracks()[0];
            audioTrack.enabled = !audioTrack.enabled;
            return !audioTrack.enabled; // return true if muted
        }
        return false;
    }

    createPeerConnection(targetUserId, isInitiator) {
        if (this.peerConnections[targetUserId]) return this.peerConnections[targetUserId];

        const pc = new RTCPeerConnection(this.iceServers);
        this.peerConnections[targetUserId] = pc;

        // Add our local stream tracks to the connection
        if (this.localStream) {
            this.localStream.getTracks().forEach(track => {
                pc.addTrack(track, this.localStream);
            });
        }

        // Handle incoming ICE candidates
        pc.onicecandidate = (event) => {
            if (event.candidate) {
                this.sendSignal({
                    type: 'ice',
                    target: targetUserId,
                    candidate: event.candidate
                });
            }
        };

        // Handle incoming audio streams
        pc.ontrack = (event) => {
            console.log('Received track from', targetUserId);
            let audioElement = document.getElementById('audio-' + targetUserId);
            if (!audioElement) {
                audioElement = document.createElement('audio');
                audioElement.id = 'audio-' + targetUserId;
                audioElement.autoplay = true;
                this.audioContainer.appendChild(audioElement);
            }
            audioElement.srcObject = event.streams[0];
        };

        if (isInitiator) {
            pc.createOffer().then(offer => {
                return pc.setLocalDescription(offer);
            }).then(() => {
                this.sendSignal({
                    type: 'offer',
                    target: targetUserId,
                    sdp: pc.localDescription
                });
            }).catch(e => console.error('Error creating offer', e));
        }

        return pc;
    }

    handleOffer(signal) {
        // If we receive an offer but we're not in the call, ignore it.
        // Or we could auto-join/prompt. For now, assume we must be in call to receive.
        if (!this.localStream) return;

        const targetUserId = signal.from;
        // The sender targeted us specifically? (Optional, if we want to filter)
        if (signal.target && signal.target !== this.currentUserId) return;

        const pc = this.createPeerConnection(targetUserId, false);
        pc.setRemoteDescription(new RTCSessionDescription(signal.sdp)).then(() => {
            return pc.createAnswer();
        }).then(answer => {
            return pc.setLocalDescription(answer);
        }).then(() => {
            this.sendSignal({
                type: 'answer',
                target: targetUserId,
                sdp: pc.localDescription
            });
        }).catch(e => console.error('Error handling offer', e));
    }

    handleAnswer(signal) {
        if (signal.target && signal.target !== this.currentUserId) return;
        
        const pc = this.peerConnections[signal.from];
        if (pc) {
            pc.setRemoteDescription(new RTCSessionDescription(signal.sdp))
                .catch(e => console.error('Error setting remote description from answer', e));
        }
    }

    handleIceCandidate(signal) {
        if (signal.target && signal.target !== this.currentUserId) return;

        const pc = this.peerConnections[signal.from];
        if (pc) {
            pc.addIceCandidate(new RTCIceCandidate(signal.candidate))
                .catch(e => console.error('Error adding ice candidate', e));
        }
    }

    handlePeerLeft(userId) {
        const pc = this.peerConnections[userId];
        if (pc) {
            pc.close();
            delete this.peerConnections[userId];
        }
        const audioElement = document.getElementById('audio-' + userId);
        if (audioElement) {
            audioElement.remove();
        }
    }

    sendSignal(payload) {
        this.stompClient.send("/app/voice/" + this.sessionId + "/signal", {}, JSON.stringify(payload));
    }
}
