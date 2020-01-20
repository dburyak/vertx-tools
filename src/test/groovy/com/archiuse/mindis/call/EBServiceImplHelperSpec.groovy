package com.archiuse.mindis.call

import spock.lang.Specification

import static com.archiuse.mindis.call.ServiceType.CALL
import static com.archiuse.mindis.call.ServiceType.PUB_SUB_TOPIC
import static com.archiuse.mindis.call.ServiceType.REQUEST_RESPONSE

class EBServiceImplHelperSpec extends Specification {

    EBServiceImplHelper ebServiceImplHelper = new EBServiceImplHelper()

    def 'buildEbServiceName builds service name correctly'() {
        setup:
        ebServiceImplHelper.ebServiceNameActionSeparator = sep

        when:
        def serviceName = ebServiceImplHelper.buildEbServiceName(rcv, action)

        then:
        noExceptionThrown()
        serviceName == expectedServiceName

        where:
        rcv        | action   | sep || expectedServiceName
        'receiver' | 'action' | '/' || 'receiver/action'
        'receiver' | 'action' | '-' || 'receiver-action'
        'a/b/c/d'  | 'action' | '/' || 'a/b/c/d/action'
        '/a/b/c/d' | 'action' | '/' || '/a/b/c/d/action'
    }

    def 'buildEbServiceName throws correctly'() {
        setup:
        ebServiceImplHelper.ebServiceNameActionSeparator = sep

        when:
        ebServiceImplHelper.buildEbServiceName(rcv, action)

        then:
        def e = thrown(expectedException)
        e
        expectedException.isAssignableFrom(e.class)

        where:
        rcv      | action   | sep  || expectedException
        null     | 'action' | '-'  || NullPointerException
        'rcv'    | null     | '-'  || NullPointerException
        'rcv'    | 'action' | null || NullPointerException
        '/a/b/c' | 'd/e'    | '/'  || CallSetupException
    }

    def 'buildEbServiceAddr builds eb addr correctly'() {
        setup:
        ebServiceImplHelper.ebAddrSeparator = sep

        when:
        def addr = ebServiceImplHelper.buildEbServiceAddr(rcv, action, type)

        then:
        noExceptionThrown()
        addr == expectedAddr

        where:
        rcv     | action | sep | type             || expectedAddr
        'rcv'   | 'act'  | ':' | CALL             || "${CALL.typeName}:rcv:act"
        'rcv'   | 'act'  | ':' | REQUEST_RESPONSE || "${REQUEST_RESPONSE.typeName}:rcv:act"
        'rcv'   | 'act'  | ':' | PUB_SUB_TOPIC    || "${PUB_SUB_TOPIC.typeName}:rcv:act"
        'a:b:c' | 'act'  | ':' | PUB_SUB_TOPIC    || "${PUB_SUB_TOPIC.typeName}:a:b:c:act"
    }

    def 'buildEbServiceAddr throws correctly'() {
        setup:
        ebServiceImplHelper.ebAddrSeparator = sep

        when:
        ebServiceImplHelper.buildEbServiceAddr(rcv, action, type)

        then:
        def e = thrown(expectedException)
        e
        expectedException.isAssignableFrom(e.class)

        where:
        rcv   | action | sep  | type || expectedException
        null  | 'act'  | ':'  | CALL || NullPointerException
        'rcv' | null   | ':'  | CALL || NullPointerException
        'rcv' | 'act'  | null | CALL || NullPointerException
        'rcv' | 'act'  | ':'  | null || NullPointerException
        'rcv' | 'a:b'  | ':'  | CALL || CallSetupException
    }

    def 'parseEbServiceAddr parses eb addr correctly'() {
        setup:
        ebServiceImplHelper.ebAddrSeparator = sep

        when:
        def list = ebServiceImplHelper.parseEbServiceAddr(addr)
        def rcv = list[0]
        def action = list[1]
        def type = list[2]

        then:
        noExceptionThrown()
        type == expectedType
        rcv == expectedRcv
        action == expectedAction

        where:
        addr                                       | sep   | expectedType | expectedRcv | expectedAction
        'mindis.verticle.call:rcv-rcv:act-act'     | ':'   | CALL         | 'rcv-rcv'   | 'act-act'
        'mindis.verticle.call::rcv-rcv::act-act'   | '::'  | CALL         | 'rcv-rcv'   | 'act-act'
        'mindis.verticle.call---rcv-rcv---act-act' | '---' | CALL         | 'rcv-rcv'   | 'act-act'
    }

    def 'parseEbServiceAddr throws correctly'() {
        setup:
        ebServiceImplHelper.ebAddrSeparator = sep

        when:
        ebServiceImplHelper.parseEbServiceAddr(addr)

        then:
        def e = thrown(expectedException)
        e
        expectedException.isAssignableFrom(e.class)

        where:
        addr                           | sep  || expectedException
        'mindis.verticle.call:rcv:act' | null || NullPointerException
        'rcv:act'                      | ':'  || MalformedEbAddressNameException
        'bad-type:rcv:act'             | ':'  || MalformedEbAddressNameException
    }
}
