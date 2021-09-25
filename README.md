# LINE-Login-demo

This repository is LINE-Login demo application.

This application gets Token from LINE Platform and just displays username and profile-picture url.

## how to run

1. [make channel](https://developers.line.biz/console/)
2. set callback URL
3. set CLIENT_ID and CLIENT_SECRET to `.env` file
4. `./gradlew bootRun`

## state

`state` is used to prevent [CSRF](https://en.wikipedia.org/wiki/Cross-site_request_forgery). We should verify
that the `state` sent to the user is the same as the `state` received from the LINE Platform.

see: https://github.com/ari1021/LINE-Login-demo/blob/main/src/main/kotlin/com/example/login/controller/LoginController.kt#L44

## nonce

`nonce` is used to prevent [replay attacks](https://en.wikipedia.org/wiki/Replay_attack). We should verify that
the `nonce` sent to the user is the same as the `nonce` in IdToken received from the LINE Platform.

There's a chance of replay attacks when we use implicit flow(deprecated), or send to token from client to
server, or etc.

## PKCE

`code_verifier`, `code_challenge`, and `code_challenge_method` are used to prevent code injection. We should do
the following:

1. generate random as `code_verifier`
1. generate `code_challenge` from `code_verifier` with `code_challenge_method`
1. send authorization request with `code_challenge` and `code_challenge_method`
1. token request with `code_verifier`

then authorization server(not ourselves) verify `code_verifier`.

see: https://github.com/ari1021/LINE-Login-demo/blob/main/src/main/kotlin/com/example/login/controller/LoginController.kt#L48

## reference

- https://developers.line.biz/ja/docs/line-login/integrate-line-login/
- https://developers.line.biz/ja/docs/line-login/verify-id-token/
- https://github.com/line/line-login-starter
- https://developers.line.biz/ja/docs/line-login/integrate-pkce/
