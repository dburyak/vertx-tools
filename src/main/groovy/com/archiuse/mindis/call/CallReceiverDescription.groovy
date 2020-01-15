package com.archiuse.mindis.call

trait CallReceiverDescription {
    String receiverName
    Set<String> actions = new LinkedHashSet<>()
}
