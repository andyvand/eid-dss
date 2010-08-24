/*
 * eID Digital Signature Service Project.
 * Copyright (C) 2009-2010 FedICT.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version
 * 3.0 as published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, see 
 * http://www.gnu.org/licenses/.
 */

package be.fedict.eid.dss.webapp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fedict.eid.dss.spi.DSSProtocolService;

/**
 * The base class for servlets that need to use protocol services. Manages the
 * life-cycle of the protocol services.
 * 
 * @author Frank Cornelis
 * 
 */
public abstract class AbstractProtocolServiceServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory
			.getLog(AbstractProtocolServiceServlet.class);

	private Map<String, DSSProtocolService> protocolServices;

	@SuppressWarnings("unchecked")
	@Override
	public void init(ServletConfig config) throws ServletException {
		/*
		 * We align the life-cycle of a DSSProtocolService with the life-cycle
		 * of this servlet.
		 */
		ServletContext servletContext = config.getServletContext();
		Map<String, String> protocolServiceClasses = StartupServletContextListener
				.getProtocolServices(servletContext);
		this.protocolServices = new HashMap<String, DSSProtocolService>();
		for (Map.Entry<String, String> protocolServiceEntry : protocolServiceClasses
				.entrySet()) {
			String contextPath = protocolServiceEntry.getKey();
			String protocolServiceClassName = protocolServiceEntry.getValue();
			Class<? extends DSSProtocolService> protocolServiceClass;
			try {
				protocolServiceClass = (Class<? extends DSSProtocolService>) Class
						.forName(protocolServiceClassName);
			} catch (ClassNotFoundException e) {
				LOG.error("protocol service class not found: "
						+ protocolServiceClassName);
				continue;
			}
			DSSProtocolService dssProtocolService;
			try {
				dssProtocolService = protocolServiceClass.newInstance();
			} catch (Exception e) {
				LOG.error("could not create an instance of the protocol service class: "
						+ protocolServiceClassName);
				continue;
			}
			dssProtocolService.init(servletContext);
			this.protocolServices.put(contextPath, dssProtocolService);
		}
	}

	@Override
	protected final void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		handleRequest(request, response);
	}

	@Override
	protected final void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		handleRequest(request, response);
	}

	protected abstract void handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException;

	/**
	 * Gives back the protocol service for the given protocol context path.
	 * 
	 * @param contextPath
	 * @return
	 */
	protected DSSProtocolService findProtocolService(String contextPath) {
		return this.protocolServices.get(contextPath);
	}

	protected String getRequiredInitParameter(ServletConfig config,
			String initParamName) throws ServletException {
		String value = config.getInitParameter(initParamName);
		if (null == value) {
			throw new ServletException(initParamName + " init-param required");
		}
		return value;
	}
}