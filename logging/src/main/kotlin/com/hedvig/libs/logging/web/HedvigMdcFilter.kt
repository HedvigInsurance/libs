package com.hedvig.libs.logging.web

import com.hedvig.libs.logging.mdc.MdcStack
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

class HedvigMdcFilter(
    private val prefix: String = "hedvig",
    private val headers: List<Header> = listOf(
        Header("Hedvig.token", "memberId"),
        Header("Accept-Language", "locale"),
    )
): Filter {

    data class Header(
        val name: String,
        val mdcKey: String
    )

    override fun init(config: FilterConfig) {
    }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        request as HttpServletRequest
        val context = mutableMapOf<String, String>()
        for (header in headers) {
            request.getHeader(header.name)?.let { value ->
                context["${prefix}.${header.mdcKey}"] = value
            }
        }

        val stack = MdcStack.push(context)
        try {
            chain.doFilter(request, response)
        } finally {
            stack.restore()
        }
    }

    override fun destroy() {
    }
}