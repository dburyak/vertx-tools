package com.dburyak.vertx.gcp.pubsub;

import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

import java.util.Map;

public interface PubSub {

    Single<String> publish(String topic, PubsubMessage msg);

    default Single<String> publish(TopicName topic, PubsubMessage msg) {
        return publish(topic.toString(), msg);
    }

    Single<String> publish(String topic, String msg);

    Single<String> publish(String topic, String msg, Map<String, String> attributes);

    default Single<String> publish(TopicName topic, String msg) {
        return publish(topic.toString(), msg);
    }

    default Single<String> publish(TopicName topic, String msg, Map<String, String> attributes) {
        return publish(topic.toString(), msg, attributes);
    }

    Single<String> publish(String topic, byte[] msg);

    Single<String> publish(String topic, byte[] msg, Map<String, String> attributes);

    default Single<String> publish(TopicName topic, byte[] msg) {
        return publish(topic.toString(), msg);
    }

    default Single<String> publish(TopicName topic, byte[] msg, Map<String, String> attributes) {
        return publish(topic.toString(), msg, attributes);
    }

    Observable<DeliverableMsg> subscribe(String subscription);

    default Observable<DeliverableMsg> subscribe(ProjectSubscriptionName subscription) {
        return subscribe(subscription.toString());
    }
}
