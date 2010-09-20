/*
 * eID Digital Signature Service Project.
 * Copyright (C) 2010 Frank Cornelis.
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

package be.fedict.eid.dss.model.bean;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import be.fedict.eid.dss.entity.XmlSchemaEntity;
import be.fedict.eid.dss.model.ExistingXmlSchemaException;
import be.fedict.eid.dss.model.InvalidXmlSchemaException;
import be.fedict.eid.dss.model.XmlSchemaManager;

@Stateless
public class XmlSchemaManagerBean implements XmlSchemaManager {

	private static final Log LOG = LogFactory
			.getLog(XmlSchemaManagerBean.class);

	@PersistenceContext
	private EntityManager entityManager;

	public List<XmlSchemaEntity> getXmlSchemas() {
		return XmlSchemaEntity.getAll(this.entityManager);
	}

	public void add(String revision, InputStream xsdInputStream)
			throws InvalidXmlSchemaException, ExistingXmlSchemaException {
		byte[] xsd;
		try {
			xsd = IOUtils.toByteArray(xsdInputStream);
		} catch (IOException e) {
			throw new RuntimeException("IO error: " + e.getMessage(), e);
		}
		ByteArrayInputStream schemaInputStream = new ByteArrayInputStream(xsd);
		SchemaFactory schemaFactory = SchemaFactory
				.newInstance("http://www.w3.org/2001/XMLSchema");
		StreamSource schemaSource = new StreamSource(schemaInputStream);
		try {
			schemaFactory.newSchema(schemaSource);
		} catch (SAXException e) {
			throw new InvalidXmlSchemaException();
		}

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
				.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder;
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("DOM error: " + e.getMessage(), e);
		}
		schemaInputStream = new ByteArrayInputStream(xsd);
		Document schemaDocument;
		try {
			schemaDocument = documentBuilder.parse(schemaInputStream);
		} catch (Exception e) {
			throw new RuntimeException("DOM error: " + e.getMessage(), e);
		}
		String namespace = schemaDocument.getDocumentElement().getAttribute(
				"targetNamespace");
		LOG.debug("namespace: " + namespace);

		XmlSchemaEntity existingXmlSchemaEntity = this.entityManager.find(
				XmlSchemaEntity.class, namespace);
		if (null != existingXmlSchemaEntity) {
			throw new ExistingXmlSchemaException();
		}

		XmlSchemaEntity xmlSchemaEntity = new XmlSchemaEntity(namespace,
				revision, xsd);
		this.entityManager.persist(xmlSchemaEntity);
	}

	public void delete(String namespace) {
		XmlSchemaEntity xmlSchemaEntity = this.entityManager.find(
				XmlSchemaEntity.class, namespace);
		this.entityManager.remove(xmlSchemaEntity);
	}
}