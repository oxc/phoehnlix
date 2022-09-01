package de.esotechnik.phoehnlix.frontend.google

import csstype.Color
import mui.material.SvgIcon
import mui.system.sx
import react.FC
import react.Props
import react.dom.svg.FillRule
import react.dom.svg.ReactSVG.g
import react.dom.svg.ReactSVG.path

/**
 * @author Bernhard Frauendienst
 */
private val blue = Color("#4285f4").toString()
private val green = Color("#34a853").toString()
private val yellow = Color("#fbbc05").toString()
private val red = Color("#ea4335").toString()

val googleIcon = FC<Props> {
  SvgIcon {
    g {
      id = "Google-Button"
      transform = "translate(-11,-11)"
      fill = "none"
      fillRule = FillRule.evenodd
      stroke = "none"
      strokeWidth = 1.0
      g {
        transform = "translate(-1,-1)"
        g {
          transform = "translate(15,15)"
          path {
            fill = blue
            d = "m 17.64,9.2045455 c 0,-0.6381819 -0.05727,-1.2518182 -0.163636,-1.8409091 H 9 V 10.845 h 4.843636 C 13.635,11.97 13.000909,12.923182 12.047727,13.561364 v 2.258182 h 2.908637 C 16.658182,14.252727 17.64,11.945455 17.64,9.2045455 Z"
          }
          path {
            fill = green
            d = "m 9,18 c 2.43,0 4.467273,-0.805909 5.956364,-2.180454 L 12.047727,13.561364 C 11.241818,14.101364 10.210909,14.420455 9,14.420455 6.6559091,14.420455 4.6718182,12.837273 3.9640909,10.71 H 0.95727273 v 2.331818 C 2.4381818,15.983182 5.4818182,18 9,18 Z"
          }
          path {
            fill = yellow
            d = "M 3.9640909,10.71 C 3.7840909,10.17 3.6818182,9.5931818 3.6818182,9 c 0,-0.5931818 0.1022727,-1.17 0.2822727,-1.71 V 4.9581818 H 0.95727273 C 0.34772727,6.1731818 0,7.5477273 0,9 c 0,1.452273 0.34772727,2.826818 0.95727273,4.041818 z"
          }
          path {
            fill = red
            d = "m 9,3.5795455 c 1.321364,0 2.507727,0.4540909 3.440455,1.3459091 L 15.021818,2.3440909 C 13.463182,0.89181818 11.425909,0 9,0 5.4818182,0 2.4381818,2.0168182 0.95727273,4.9581818 L 3.9640909,7.29 C 4.6718182,5.1627273 6.6559091,3.5795455 9,3.5795455 Z"
          }
          path {
            d = "M 0,0 H 18 V 18 H 0 Z"
          }
        }
      }
    }
  }
}