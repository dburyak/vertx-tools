package com.archiuse.mindis.app

import groovy.transform.InheritConstructors

@InheritConstructors
class UnexpectedAppStateException extends LifecycleException {
    Set<AppState> expectedAppStates
}
