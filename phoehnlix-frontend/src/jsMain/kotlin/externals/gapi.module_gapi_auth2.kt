package gapi.auth2

/**
 * @author Bernhard Frauendienst
 */

external interface AuthorizeConfig {
  var client_id: String?
  var scope: String?
  var response_type: String?
  var prompt: String?
  var access_type: String? // not documented but works
}

external interface AuthorizeResponse {
  var access_token: String?
  var id_token: String?
  var code: String?
  var scope: String?
  var first_issued_at: Long?
  var expires_at: Long?
  var error: String?
  var error_subtype: String?
}

// not modelling this is as module/object because we lazy-load it
external interface GapiAuth2 {
  fun authorize(params: AuthorizeConfig, callback: (AuthorizeResponse) -> Unit)
}