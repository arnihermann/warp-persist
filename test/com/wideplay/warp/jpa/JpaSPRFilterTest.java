package com.wideplay.warp.jpa;

import com.wideplay.warp.persist.UnitOfWork;
import static org.easymock.EasyMock.*;
import org.testng.annotations.Test;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityManager;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: dprasanna
 * Date: 11/10/2007
 * Time: 11:16:17
 * <p/>
 *
 * Tests the SPR filters behavior in creating, delegating to filter chain and finally destroying an EM.
 *
 * Tests in exceptional cases too.
 *
 * @author dprasanna
 * @since 1.0
 */
public class JpaSPRFilterTest {

    @Test
    public final void testDoFilter() throws IOException, ServletException {
//        //create our spr filter and set it up
        SessionPerRequestFilter filter = new SessionPerRequestFilter();
        SessionPerRequestFilter.setUnitOfWork(UnitOfWork.REQUEST);

        EntityManagerFactory emfMock = createStrictMock(EntityManagerFactory.class);
        EntityManager emMock = createStrictMock(EntityManager.class);
        new EntityManagerFactoryHolder().setEntityManagerFactory(emfMock); 

        //create our mock servlet context artifacts
        FilterChain filterChain = createStrictMock(FilterChain.class);
        ServletRequest mockRequest = createStrictMock(ServletRequest.class);
        ServletResponse mockResponse = createStrictMock(ServletResponse.class);


        //setup expected behavior
        filterChain.doFilter(mockRequest, mockResponse);
        expect(emfMock.createEntityManager()).andReturn(emMock);
        expect(emMock.isOpen()).andReturn(true);
        emMock.close();

        //prepare mocks for battle!
        replay(filterChain, mockRequest, mockResponse, emfMock, emMock);

        //test filter
        filter.doFilter(mockRequest, mockResponse, filterChain);

        verify(filterChain, mockRequest, mockResponse, emfMock, emMock);
    }

    @Test
    public final void testDoFilterWithException() throws IOException, ServletException {

//        //create our spr filter and set it up
        SessionPerRequestFilter filter = new SessionPerRequestFilter();
        SessionPerRequestFilter.setUnitOfWork(UnitOfWork.REQUEST);

        EntityManagerFactory emfMock = createStrictMock(EntityManagerFactory.class);
        EntityManager emMock = createStrictMock(EntityManager.class);
        new EntityManagerFactoryHolder().setEntityManagerFactory(emfMock);

        //create our mock servlet context artifacts
        FilterChain filterChain = createStrictMock(FilterChain.class);
        ServletRequest mockRequest = createStrictMock(ServletRequest.class);
        ServletResponse mockResponse = createStrictMock(ServletResponse.class);


        //setup expected behavior
        filterChain.doFilter(mockRequest, mockResponse);
        expectLastCall()
                .andThrow(new ServletException());

        expect(emfMock.createEntityManager())
                .andReturn(emMock);

        expect(emMock.isOpen())
                .andReturn(true);
        emMock.close();

        //prepare mocks for battle!
        replay(filterChain, mockRequest, mockResponse, emfMock, emMock);

        //test filter
        ServletException e = null;
        try {
            filter.doFilter(mockRequest, mockResponse, filterChain);
        } catch(ServletException se) {
            e = se;
        }

        assert e != null : "ServletException was not propagated as expected";

        verify(filterChain, mockRequest, mockResponse, emfMock, emMock);
    }
}
