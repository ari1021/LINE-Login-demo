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
        const val CODE_VERIFIER = "code_verifier"
        const val ID_TOKEN = "id_token"
    }

    @GetMapping("/")
    fun index(): String {
        return "index"
    }

    @GetMapping("/login")
    fun login(httpSession: HttpSession): String {
        val state = Utils().getRandom() // to avoid csrf
        val codeVerifier = Utils().getRandom() // to avoid code injection
        httpSession.setAttribute(STATE, state)
        httpSession.setAttribute(CODE_VERIFIER, codeVerifier)
        val codeChallenge = Utils().sha256WithBase64UrlEncoded(codeVerifier)
        val url = lineAPIService.getLineLoginUrl(state, codeChallenge, listOf("profile", "openid"))
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
        val codeVerifier = httpSession.getAttribute(CODE_VERIFIER)
//        hit api to get Token with code and code_verifier not to prevent code injection(PKCE)
        val response = lineAPIService.getToken(code, codeVerifier as String) ?: return "fail"
        httpSession.setAttribute(ID_TOKEN, response.idToken)
        return "redirect:/success"
    }

    @GetMapping("/success")
    fun success(httpSession: HttpSession, model: Model): String {
        val idToken = httpSession.getAttribute(ID_TOKEN)
//        check if the idToken is valid or not
        if (!lineAPIService.verifyIdToken(idToken as String)) {
            return "fail"
        }
        val decodedIdToken = lineAPIService.decodeIdToken(idToken)
        model.addAttribute("name", decodedIdToken.name)
        model.addAttribute("picture", decodedIdToken.picture)
        return "success"
    }
}
