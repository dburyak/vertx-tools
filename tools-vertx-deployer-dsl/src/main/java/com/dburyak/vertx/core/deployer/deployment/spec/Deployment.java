package com.dburyak.vertx.core.deployer.deployment.spec;

import lombok.Builder;
import lombok.Data;

import java.util.Objects;


/*
// probably javascript and/or yaml formats should be supported too; only groovy dsl for now

// what is this needed for????
// - map endpoint/eb-endpoint name -> eb address name
// - configure default delivery options for all/actor/endpoint
// - configure HTTP api

// TODO: add Auth bean that provides auth token, several impls
//   - periodically pull it from user-service or/and auth service and cache
//   - use token from env

deployment {
  verticles {
    verticle1 {
      type VERTX_EVENT_BUS              (default: VERTX_EVENT_BUS)
      instances 2 * NUM_CPU             (default: 1)
      producer "com.blah.blah.Verticle1.Producer"
      in { // actions this verticle can perform, addresses it listens to
        baseAddr "/actor1"                (default: "")
        "/action1" {
          addr "/actor1/action1"          (default: same as action name)
        }
        "/actor1/action2" {
          addr "/actor1/action2_overridden"
        }
        "/verticle1/action3"              (same as action)
      }
      out { // configuration for messages sent out by this verticle (should be optional)
        "/actor2/action" {
          localOnly true
        }
      }
    }
    actor2(type: HTTP_OUT) {
      type HTTP_OUT
      instances NUM_CPU - 1
      url env.SOME_SERVICE_URL
      headers ALL                       (incoming headers to use)
      auth {
        enabled true
        header env.AUTH_HEADER
      }
      in {
        "/users" {
          method GET
          headers ["X-One", "X-Two", env.AUTH_HEADER]
        }
        "/users/{userId}" {
          url "https://overridden.com/api/v2"
          method GET
        }
        "/users/post" {
          method POST
          path "/users"
          headers NONE
          auth {
            enabled false
          }
        }
      }
    }
    actor3(type: HTTP_IN) {
      instances 4
      basePath "/api/v3"
      headers ALL                             (default: ALL)
      auth {
        enabled env.ACTOR_3_AUTH_ENABLED
        roles ["manager", "admin"]
      }
      out {
        "/books"(method: GET, action: "/actor1/action1")      (default: action with same name)
        "/books/{bookId}"(method: GET)
        "/books/create" {
          method POST
          path "/books"
        }
      }
    }
    actor4(type: GRPC_OUT) {
    }
  }
}
*/

@Data
@Builder(toBuilder = true)
public class Deployment {

    @Builder.Default
    private final Verticles verticles = Verticles.builder().build();

    public static class DeploymentBuilder {
        public Deployment build() {
            Objects.requireNonNull(verticles$value);
            return new Deployment(verticles$value);
        }
    }
}
