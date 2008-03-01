package com.wideplay.warp.db4o;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import static org.easymock.EasyMock.*;
import org.testng.annotations.Test;

import com.db4o.ObjectContainer;
import com.db4o.ObjectServer;
import com.db4o.ext.ExtObjectContainer;
import com.wideplay.warp.persist.UnitOfWork;

/**
 * 
 * @author Jeffrey Chung (lwbruce@gmail.com)
 */
@Test(suiteName = "db4o")
public class Db4oSPRFilterTest {

	@Test
	public final void testDoFilter() throws IOException, ServletException {
		SessionPerRequestFilter filter = new SessionPerRequestFilter();
		SessionPerRequestFilter.setUnitOfWork(UnitOfWork.REQUEST);

		ObjectServer osMock = createMock(ObjectServer.class);
		ObjectContainer ocMock = createMock(ObjectContainer.class);
		ExtObjectContainer eocMock = createMock(ExtObjectContainer.class);
		new ObjectServerHolder().setObjectServer(osMock);

		FilterChain mockFilterChain = createMock(FilterChain.class);
		ServletRequest mockRequest = createMock(ServletRequest.class);
		ServletResponse mockResponse = createMock(ServletResponse.class);

		mockFilterChain.doFilter(mockRequest, mockResponse);
		expect(osMock.openClient()).andReturn(ocMock);
		expect(ocMock.ext()).andReturn(eocMock);
		expect(eocMock.isClosed()).andReturn(false);
		expect(ocMock.close()).andReturn(true);

		replay(mockFilterChain, mockRequest, mockResponse, osMock, ocMock, eocMock);

		filter.doFilter(mockRequest, mockResponse, mockFilterChain);

		verify(mockFilterChain, mockRequest, mockResponse, osMock, ocMock, eocMock);
	}

	@Test
	public final void testDoFilterWithException() throws IOException, ServletException {
		SessionPerRequestFilter filter = new SessionPerRequestFilter();
		SessionPerRequestFilter.setUnitOfWork(UnitOfWork.REQUEST);

		ObjectServer osMock = createMock(ObjectServer.class);
		ObjectContainer ocMock = createMock(ObjectContainer.class);
		ExtObjectContainer eocMock = createMock(ExtObjectContainer.class);
		new ObjectServerHolder().setObjectServer(osMock);

		FilterChain mockFilterChain = createMock(FilterChain.class);
		ServletRequest mockRequest = createMock(ServletRequest.class);
		ServletResponse mockResponse = createMock(ServletResponse.class);

		mockFilterChain.doFilter(mockRequest, mockResponse);
		expectLastCall().andThrow(new ServletException());
		expect(osMock.openClient()).andReturn(ocMock);
		expect(ocMock.ext()).andReturn(eocMock);
		expect(eocMock.isClosed()).andReturn(false);
		expect(ocMock.close()).andReturn(true);

		replay(mockFilterChain, mockRequest, mockResponse, osMock, ocMock, eocMock);

		ServletException e = null;
		try {
			filter.doFilter(mockRequest, mockResponse, mockFilterChain);
		} catch (ServletException se) {
			e = se;
		}

		assert e != null : "ServletException was not propagated as expected";

		verify(mockFilterChain, mockRequest, mockResponse, osMock, ocMock, eocMock);
	}
}
