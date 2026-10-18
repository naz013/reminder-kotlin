package com.github.naz013.common.system

interface BuildInfo {
  val isPro: Boolean
  /** True only for the paid PRO build; unlike [isPro] it is not unlocked by the trial. */
  val isBuildPro: Boolean
  val isDebug: Boolean
  val applicationId: String
  val buildDate: String
  val googleSignInServerClientId: String
}
