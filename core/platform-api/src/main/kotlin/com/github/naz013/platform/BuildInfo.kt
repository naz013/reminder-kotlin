package com.github.naz013.platform

interface BuildInfo {
  /** Read at call time; includes the temporary PRO trial. Never cache it. */
  val isPro: Boolean
  /** True only for the paid PRO build; unlike [isPro] it is not unlocked by the trial. */
  val isBuildPro: Boolean
  val isDebug: Boolean
  val applicationId: String
  val buildDate: String
  val googleSignInServerClientId: String
}
