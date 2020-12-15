package com.dburyak.vertx.core

import com.archiuse.mindis.call.ServiceHelper
import io.micronaut.context.ApplicationContext
import io.micronaut.context.BeanRegistration
import io.micronaut.inject.BeanIdentifier
import spock.lang.Timeout

import static java.util.concurrent.TimeUnit.SECONDS

@Timeout(value = 2, unit = SECONDS)
class VerticleSpec extends VertxRxJavaSpec {
    Verticle verticle = Spy(Verticle)

    ApplicationContext verticleBeanCtx = Mock(ApplicationContext)
    ServiceHelper serviceHelper = Mock(ServiceHelper)
    BeanRegistration beanCtxBeanReg = Mock(BeanRegistration)
    BeanRegistration verticleBeanReg = Mock(BeanRegistration)
    BeanIdentifier verticleBeanId = Mock(BeanIdentifier)
    BeanIdentifier beanCtxBeanId = Mock(BeanIdentifier)

    void setup() {
        verticle.@verticleBeanCtx = verticleBeanCtx
    }
}
