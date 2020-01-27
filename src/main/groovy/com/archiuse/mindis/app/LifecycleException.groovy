package com.archiuse.mindis.app

import com.archiuse.mindis.MindisRuntimeException
import groovy.transform.InheritConstructors

@InheritConstructors
class LifecycleException extends MindisRuntimeException {
    AppState appState
}
