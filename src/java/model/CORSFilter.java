/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author hp
 */
@WebFilter("/*")
public class CORSFilter implements Filter{

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    
        HttpServletRequest hsRequest = (HttpServletRequest) request;
        HttpServletResponse hsResponse = (HttpServletResponse) response;
        
        hsResponse.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        hsResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        hsResponse.setHeader("Access-Control-Allow-Headers", "Content-Type");
        hsResponse.setHeader("Access-Control-Allow-Credentials", "true");
        
        if("OPTIONS".equalsIgnoreCase(hsRequest.getMethod())){
            hsResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        
        chain.doFilter(request, response);
        
    }

    @Override
    public void destroy() {}
    
}
