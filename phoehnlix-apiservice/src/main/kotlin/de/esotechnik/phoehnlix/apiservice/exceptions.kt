package de.esotechnik.phoehnlix.apiservice

import java.lang.RuntimeException

/**
 * @author Bernhard Frauendienst
 */

class UnauthorizedException(message: String) : RuntimeException(message)

class ProfileIncompleteException(message: String) : RuntimeException(message)