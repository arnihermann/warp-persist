/**
 * Copyright (C) 2008 Wideplay Interactive.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wideplay.warp.jpa;

/**
 * Created by IntelliJ IDEA.
 * User: Dhanji R. Prasanna (dhanji@gmail.com)
 * Date: 11/10/2007
 * Time: 11:16:17
 * <p/>
 *
 * Tests the SPR filters behavior in creating, delegating to filter chain and finally destroying an EM.
 *
 * Tests in exceptional cases too.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @since 1.0
 */
public class JpaSPRFilterTest {

// TODO test SessionFilter instead

//    @Test
//    public final void testDoFilter() throws IOException, ServletException {
////        //create our spr filter and set it up
//        SessionPerRequestFilter filter = new SessionPerRequestFilter();
//        JpaLocalTxnInterceptor.setUnitOfWork(UnitOfWork.REQUEST);
//
//        EntityManagerFactory emfMock = createStrictMock(EntityManagerFactory.class);
//        EntityManager emMock = createStrictMock(EntityManager.class);
//        new EntityManagerFactoryHolder().setEntityManagerFactory(emfMock);
//
//        //create our mock servlet context artifacts
//        FilterChain filterChain = createStrictMock(FilterChain.class);
//        ServletRequest mockRequest = createStrictMock(ServletRequest.class);
//        ServletResponse mockResponse = createStrictMock(ServletResponse.class);
//
//
//        //setup expected behavior
//        filterChain.doFilter(mockRequest, mockResponse);
//        expect(emfMock.createEntityManager()).andReturn(emMock);
//        expect(emMock.isOpen()).andReturn(true);
//        emMock.close();
//
//        //prepare mocks for battle!
//        replay(filterChain, mockRequest, mockResponse, emfMock, emMock);
//
//        //test filter
//        filter.doFilter(mockRequest, mockResponse, filterChain);
//
//        verify(filterChain, mockRequest, mockResponse, emfMock, emMock);
//    }
//
//    @Test
//    public final void testDoFilterWithException() throws IOException, ServletException {
//
////        //create our spr filter and set it up
//        SessionPerRequestFilter filter = new SessionPerRequestFilter();
//        JpaLocalTxnInterceptor.setUnitOfWork(UnitOfWork.REQUEST);
//
//        EntityManagerFactory emfMock = createStrictMock(EntityManagerFactory.class);
//        EntityManager emMock = createStrictMock(EntityManager.class);
//        new EntityManagerFactoryHolder().setEntityManagerFactory(emfMock);
//
//        //create our mock servlet context artifacts
//        FilterChain filterChain = createStrictMock(FilterChain.class);
//        ServletRequest mockRequest = createStrictMock(ServletRequest.class);
//        ServletResponse mockResponse = createStrictMock(ServletResponse.class);
//
//
//        //setup expected behavior
//        filterChain.doFilter(mockRequest, mockResponse);
//        expectLastCall()
//                .andThrow(new ServletException());
//
//        expect(emfMock.createEntityManager())
//                .andReturn(emMock);
//
//        expect(emMock.isOpen())
//                .andReturn(true);
//        emMock.close();
//
//        //prepare mocks for battle!
//        replay(filterChain, mockRequest, mockResponse, emfMock, emMock);
//
//        //test filter
//        ServletException e = null;
//        try {
//            filter.doFilter(mockRequest, mockResponse, filterChain);
//        } catch(ServletException se) {
//            e = se;
//        }
//
//        assert e != null : "ServletException was not propagated as expected";
//
//        verify(filterChain, mockRequest, mockResponse, emfMock, emMock);
//    }
}
