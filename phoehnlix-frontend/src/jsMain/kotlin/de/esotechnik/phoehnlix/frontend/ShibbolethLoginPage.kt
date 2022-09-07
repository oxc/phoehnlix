package de.esotechnik.phoehnlix.frontend

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.js.get
import mui.material.Typography
import react.FC
import react.Props
import react.router.useParams
import react.useContext
import react.useEffect

val ShibbolethLoginPage = FC<Props> { props ->
  val context = useContext(PhoehnlixContext)

  val params = useParams()
  val profileId =  params["profileId"]?.toIntOrNull()

  useEffect(profileId) {
    if (profileId == null) {
      console.error("No profileId given")
      return@useEffect
    }
    with(context) {
      val mainScope = MainScope() + CoroutineName("ShibbolethLogin")
      mainScope.launch {
        val login = api.login.shibboleth(profileId)
        update(
          apiToken = login.apiToken,
          profile = login.profile,
          profileDraft = login.profileDraft
        )
      }
    }
  }

  if (profileId == context.currentProfile?.id) {
    Typography {
      +"Welcome as ${context.currentProfile?.name}"
    }
      /*
    Navigate {
      to = "/"
    }
     */
  }
}
