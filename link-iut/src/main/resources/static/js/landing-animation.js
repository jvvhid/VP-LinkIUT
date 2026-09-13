document.addEventListener('DOMContentLoaded', () => {
    const canvas = document.getElementById('ascii-canvas');
    if (!canvas) return;
    
    const ctx = canvas.getContext('2d');
    const image = document.getElementById('source-image');
    const container = document.getElementById('animation-container');
    
    // Accessibility check
    const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    
    let particles = [];
    let mouse = { x: -1000, y: -1000, radius: 150 };
    let startTime = 0;
    
    // ASCII density string from dark to light
    const density = '.:-=+*#%@';
    
    class Particle {
        constructor(x, y, color, char, isEdge, canvasHeight) {
            this.baseX = x;
            this.baseY = y;
            this.color = color;
            this.char = char;
            this.size = 8;
            this.vx = 0;
            this.vy = 0;
            this.friction = 0.85;
            this.springFactor = 0.1;
            
            // Build Animation Logic
            // Bottom particles (y ~ canvasHeight) get a low delay. Top particles get up to 3000ms delay.
            let normalizedHeight = (canvasHeight - y) / canvasHeight;
            this.introDelay = (normalizedHeight * 3000) + (Math.random() * 500);
            
            // Start position: high above the screen with some horizontal scatter
            this.x = x + (Math.random() - 0.5) * 200;
            this.y = -100 - (Math.random() * 300);
            this.isBuilt = false;
        }
        
        update(time) {
            // Wait for the build delay
            if (time < this.introDelay) {
                return;
            }
            this.isBuilt = true;

            // Mouse Repulsion
            let dx = mouse.x - this.x;
            let dy = mouse.y - this.y;
            let distance = Math.sqrt(dx * dx + dy * dy);
            
            if (distance < mouse.radius) {
                let forceDirectionX = dx / distance;
                let forceDirectionY = dy / distance;
                let force = (mouse.radius - distance) / mouse.radius;
                let directionX = forceDirectionX * force * 8;
                let directionY = forceDirectionY * force * 8;
                
                this.vx -= directionX;
                this.vy -= directionY;
            }
            
            let targetX = this.baseX;
            let targetY = this.baseY;
            
            // Spring Physics towards target
            this.vx += (targetX - this.x) * this.springFactor;
            this.vy += (targetY - this.y) * this.springFactor;
            
            // Apply friction
            this.vx *= this.friction;
            this.vy *= this.friction;
            
            this.x += this.vx;
            this.y += this.vy;
        }
        
        draw(ctx) {
            if (!this.isBuilt) return; // Don't draw if not built yet
            
            ctx.fillStyle = this.color;
            ctx.fillText(this.char, this.x, this.y);
        }
    }
    
    function init() {
        startTime = performance.now();
        
        // Handle reduced motion
        if (prefersReducedMotion) {
            // Just draw the image directly
            canvas.width = container.clientWidth;
            canvas.height = container.clientHeight;
            ctx.drawImage(image, 0, 0, canvas.width, canvas.height);
            return;
        }
        
        // Setup canvas sizing
        const rect = container.getBoundingClientRect();
        canvas.width = rect.width;
        canvas.height = rect.height;
        
        // Setup offscreen canvas for image processing
        const offscreen = document.createElement('canvas');
        const octx = offscreen.getContext('2d');
        
        // Calculate scale to fit image nicely in canvas while preserving aspect ratio
        const imgRatio = image.naturalWidth / image.naturalHeight;
        const canvasRatio = canvas.width / canvas.height;
        let drawWidth, drawHeight;
        
        if (canvasRatio > imgRatio) {
            drawHeight = canvas.height;
            drawWidth = drawHeight * imgRatio;
        } else {
            drawWidth = canvas.width;
            drawHeight = drawWidth / imgRatio;
        }

        // Remove the 1.25x manual scale, the HTML canvas is now 125% size natively!
        
        offscreen.width = canvas.width;
        offscreen.height = canvas.height;
        
        // Draw image centered in offscreen canvas
        const offsetX = (canvas.width - drawWidth) / 2;
        const offsetY = (canvas.height - drawHeight) / 2;
        octx.drawImage(image, offsetX, offsetY, drawWidth, drawHeight);
        
        const imageData = octx.getImageData(0, 0, canvas.width, canvas.height);
        const data = imageData.data;
        
        particles = [];
        const step = 7; // Sampling step interval
        
        ctx.font = '8px monospace';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        
        // High Contrast factor formula
        const contrast = 70; // 0 to 255
        const factor = (259 * (contrast + 255)) / (255 * (259 - contrast));
        
        for (let y = 0; y < canvas.height; y += step) {
            for (let x = 0; x < canvas.width; x += step) {
                const index = (y * canvas.width + x) * 4;
                const alpha = data[index + 3];
                
                if (alpha > 30) { 
                    let origR = data[index];
                    let origG = data[index + 1];
                    let origB = data[index + 2];
                    
                    let r = origR;
                    let g = origG;
                    let b = origB;
                    
                    // Detect Yellow/Golden pixels (High R and G, lower B)
                    let isYellow = (origR > 80 && origG > 80 && origB < Math.min(origR, origG) * 0.75 && Math.abs(origR - origG) < 80);
                    
                    // 1. Lift shadows FIRST so they don't get crushed by contrast
                    r += 60;
                    g += 60;
                    b += 60;
                    
                    // 2. Apply High Contrast
                    r = factor * (r - 128) + 128;
                    g = factor * (g - 128) + 128;
                    b = factor * (b - 128) + 128;
                    
                    // 3. Increase Brightness by 1.5x as requested
                    r *= 1.5;
                    g *= 1.5;
                    b *= 1.5;
                    
                    // 4. Clamp values, ensuring they never drop below 80
                    r = Math.max(80, Math.min(255, r));
                    g = Math.max(80, Math.min(255, g));
                    b = Math.max(80, Math.min(255, b));
                    
                    // 5. If it was a yellow pixel, override the final color to bright red
                    if (isYellow) {
                        r = 255;
                        g = 30; // Slight bit of green so it's a glowing red rather than dark red
                        b = 30;
                    }
                    
                    // Calculate brightness (relative luminance)
                    const brightness = (0.299 * r + 0.587 * g + 0.114 * b);
                    const charIndex = Math.floor(mapRange(brightness, 0, 255, 0, density.length - 1));
                    const char = density.charAt(charIndex);
                    
                    // Convert back to int for css color string using brightened RGB
                    const color = `rgb(${Math.floor(r)}, ${Math.floor(g)}, ${Math.floor(b)})`;
                    
                    particles.push(new Particle(x, y, color, char, false, canvas.height));
                }
            }
        }
        
        animate();
    }
    
    function mapRange(value, inMin, inMax, outMin, outMax) {
        return (value - inMin) * (outMax - outMin) / (inMax - inMin) + outMin;
    }
    
    function animate(time = 0) {
        if (prefersReducedMotion) return;
        
        let elapsed = performance.now() - startTime;
        
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        
        for (let i = 0; i < particles.length; i++) {
            particles[i].update(elapsed);
            particles[i].draw(ctx);
        }
        
        requestAnimationFrame(animate);
    }
    
    // Event Listeners
    canvas.addEventListener('mousemove', (e) => {
        const rect = canvas.getBoundingClientRect();
        mouse.x = e.clientX - rect.left;
        mouse.y = e.clientY - rect.top;
    });
    
    canvas.addEventListener('mouseleave', () => {
        mouse.x = -1000;
        mouse.y = -1000;
    });
    
    window.addEventListener('resize', () => {
        // Debounce resize
        clearTimeout(window.resizeTimer);
        window.resizeTimer = setTimeout(() => {
            init();
        }, 200);
    });
    
    // Ensure image is loaded before processing
    if (image.complete) {
        init();
    } else {
        image.addEventListener('load', init);
    }
});
