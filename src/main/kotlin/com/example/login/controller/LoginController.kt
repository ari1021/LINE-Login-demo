package com.example.login.controller

import com.example.login.line.api.v2.LineAPIService
import com.example.login.utils.Utils
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import javax.servlet.http.HttpSession

@Controller
class LoginController(private val lineAPIService: LineAPIService) {
    companion object {
        const val STATE = "state"
        const val NONCE = "nonce"
        const val ID_TOKEN = "id_token"
    }

    @GetMapping("/")
    fun index(): String {
        return "index"
    }

    @GetMapping("/login")
    fun login(httpSession: HttpSession): String {
        val state = Utils().getRandom() // to avoid csrf
        val nonce = Utils().getRandom() // to avoid replay attack
        httpSession.setAttribute(STATE, state)
        httpSession.setAttribute(NONCE, nonce)
        val url = lineAPIService.getLineLoginUrl(state, nonce, listOf("profile", "openid"))
        return "redirect:$url"
    }

    @GetMapping("/auth")
    fun auth(
        httpSession: HttpSession,
        @RequestParam(value = "code", required = false) code: String?,
        @RequestParam(value = "state", required = false) state: String?,
        @RequestParam(value = "error", required = false) error: String?,
        @RequestParam(value = "error_description", required = false) errorDescription: String?
    ): String {
        if (code == null || error != null) return "fail"
//        check if the state is the same as session's state or not to prevent csrf
        if (httpSession.getAttribute(STATE) != state) return "fail"
        httpSession.removeAttribute(STATE)
//        hit api to get Token with code
        val response = lineAPIService.getToken(code) ?: return "fail"
        httpSession.setAttribute(ID_TOKEN, response.idToken)
        return "redirect:/success"
    }

    @GetMapping("/success")
    fun success(httpSession: HttpSession, model: Model): String {
        val idToken = httpSession.getAttribute(ID_TOKEN)
        val nonce = httpSession.getAttribute(NONCE)
//        check if the idToken is valid or not
//        nonce is used to prevent replay attack
        if (!lineAPIService.verifyIdToken(idToken as String, nonce as String)) return "fail"
        httpSession.removeAttribute(NONCE)
        val decodedIdToken = lineAPIService.decodeIdToken(idToken)
        model.addAttribute("name", decodedIdToken.name)
        model.addAttribute("picture", decodedIdToken.picture)
        return "success"
    }
}