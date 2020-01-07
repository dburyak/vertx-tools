package com.archiuse.mindis.call

trait CallReceiverDescription {
    String receiverAddress
    Set<String> actions = new LinkedHashSet<>()
}
