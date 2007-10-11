package com.wideplay.warp.hibernate;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: dprasanna
 * Date: 11/10/2007
 * Time: 11:16:17
 * <p/>
 * TODO: Describe me!
 *
 * @author dprasanna
 * @since 1.0
 */
public class HibernateSPRFilterTest {

//    @Test
    //TODO this is a bit difficult to test--how do I setup mocks for static APIs within Hibernate =(
    public final void testDoFilter() throws IOException, ServletException {
        throw new Error("unimplementated");
//        //create our spr filter and set it up
//        SessionPerRequestFilter filter = new SessionPerRequestFilter();
//        SessionPerRequestFilter.setUnitOfWork(UnitOfWork.REQUEST);
//
//
//        SessionFactory mockSF = createNiceMock(SessionFactory.class);
//        expect(mockSF.getCurrentSession()).andReturn(createNiceMock(Session.class));
//
//        new SessionFactoryHolder().setSessionFactory(mockSF);
//
//        //create our mock servlet context artifacts
//        FilterChain filterChain = createStrictMock(FilterChain.class);
//        ServletRequest mockRequest = createStrictMock(ServletRequest.class);
//        ServletResponse mockResponse = createStrictMock(ServletResponse.class);
//
//        //prepare mocks for battle!
//        replay(filterChain, mockRequest, mockResponse, mockSF);
//
//        //test filter
//        filter.doFilter(mockRequest, mockResponse, filterChain);
//
//        verify(filterChain);
    }
}
