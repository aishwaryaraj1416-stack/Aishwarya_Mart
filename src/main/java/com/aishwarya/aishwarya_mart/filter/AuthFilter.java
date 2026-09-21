package com.aishwarya.aishwarya_mart.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(urlPatterns = {
        "/cart",
        "/orders",
        "/wishlist",
        "/seller.html"
})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(
            javax.servlet.ServletRequest request,
            javax.servlet.ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest =
                (HttpServletRequest) request;

        HttpServletResponse httpResponse =
                (HttpServletResponse) response;

        HttpSession session =
                httpRequest.getSession(false);

        // --------------------------------------------------
        // Check whether the user is logged in
        // --------------------------------------------------

        if (session == null ||
                session.getAttribute("userId") == null) {

            httpResponse.sendRedirect(
                    httpRequest.getContextPath()
                            + "/login.html"
            );

            return;
        }

        // --------------------------------------------------
        // Seller page needs SELLER or ADMIN role
        // --------------------------------------------------

        String path = httpRequest.getRequestURI()
                .substring(
                        httpRequest.getContextPath().length()
                );

        if ("/seller.html".equals(path)) {

            String role =
                    (String) session.getAttribute("role");

            if (!"SELLER".equals(role) &&
                    !"ADMIN".equals(role)) {

                httpResponse.sendRedirect(
                        httpRequest.getContextPath()
                                + "/index.html"
                );

                return;
            }
        }

        chain.doFilter(request, response);
    }
}