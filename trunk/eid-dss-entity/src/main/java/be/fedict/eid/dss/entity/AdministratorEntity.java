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

package be.fedict.eid.dss.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Query;
import javax.persistence.Table;

@Entity
@Table(name = "dss_admin")
@NamedQueries({ @NamedQuery(name = AdministratorEntity.COUNT_ALL, query = "SELECT COUNT(*) FROM AdministratorEntity") })
public class AdministratorEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String COUNT_ALL = "dss.admin.count.all";

	private String id;

	private String name;

	public AdministratorEntity(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public AdministratorEntity() {
		super();
	}

	@Id
	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Column(nullable = false)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static boolean hasAdmins(EntityManager entityManager) {
		Query query = entityManager.createNamedQuery(COUNT_ALL);
		Long count = (Long) query.getSingleResult();
		return 0 != count;
	}
}