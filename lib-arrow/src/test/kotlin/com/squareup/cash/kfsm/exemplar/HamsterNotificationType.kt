package com.squareup.cash.kfsm.exemplar

import com.squareup.cash.kfsm.NotificationType

sealed class HamsterNotificationType : NotificationType
object Console : HamsterNotificationType()
object TheVoid : HamsterNotificationType()
object Owner : HamsterNotificationType()
