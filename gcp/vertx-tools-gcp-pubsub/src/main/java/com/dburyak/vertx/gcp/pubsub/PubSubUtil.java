package com.dburyak.vertx.gcp.pubsub;

import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.TopicName;
import jakarta.inject.Singleton;

@Singleton
public class PubSubUtil {
    public static final String FQN_PREFIX = "projects/";

    public boolean isFqn(String topicOrSubscription) {
        return topicOrSubscription.startsWith(FQN_PREFIX);
    }

    public String fqnTopic(String projectId, String topic) {
        return FQN_PREFIX + projectId + "/topics/" + topic;
    }

    public String fqnSubscription(String projectId, String subscription) {
        return FQN_PREFIX + projectId + "/subscriptions/" + subscription;
    }

    public TopicName fqnTopicName(String projectId, String topic) {
        if (isFqn(topic)) {
            return TopicName.parse(topic);
        }
        return TopicName.of(projectId, topic);
    }

    public ProjectSubscriptionName fqnSubscriptionName(String projectId, String subscription) {
        if (isFqn(subscription)) {
            return ProjectSubscriptionName.parse(subscription);
        }
        return ProjectSubscriptionName.of(projectId, subscription);
    }

    public String ensureFqnTopic(String projectId, String topic) {
        if (isFqn(topic)) {
            return topic;
        }
        return fqnTopic(projectId, topic);
    }

    public String ensureFqnSubscription(String projectId, String subscription) {
        if (isFqn(subscription)) {
            return subscription;
        }
        return fqnSubscription(projectId, subscription);
    }

    public String forceProjectForSubscription(String projectId, String subscription) {
        if (!isFqn(subscription)) {
            return fqnSubscription(projectId, subscription);
        } else if (subscription.startsWith(FQN_PREFIX + projectId + "/")) {
            return subscription;
        } else { // is FQN but not for this project
            var subscriptionName = ProjectSubscriptionName.parse(subscription);
            return subscriptionName.toBuilder()
                    .setProject(projectId)
                    .build()
                    .toString();
        }
    }

    public String topicShortName(String topic) {
        if (!isFqn(topic)) {
            return topic;
        }
        return topic.substring(topic.lastIndexOf('/') + 1);
    }

    public String subscriptionShortName(String subscription) {
        if (!isFqn(subscription)) {
            return subscription;
        }
        return subscription.substring(subscription.lastIndexOf('/') + 1);
    }
}
