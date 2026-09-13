package edu.iutdhaka.linkiut.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(MaxUploadSizeExceededException exc, HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer != null) {
            if (referer.contains("/profile")) {
                return "redirect:/profile/edit?error=FileTooLarge";
            }
            if (referer.contains("/messages")) {
                return "redirect:/messages?error=FileTooLarge";
            }
        }
        return "redirect:/feed?error=FileTooLarge";
    }
}
