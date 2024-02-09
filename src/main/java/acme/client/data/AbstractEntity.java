/*
 * AbstractEntity.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.client.data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.commons.lang3.builder.EqualsBuilder;

import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Getter
@Setter
@CustomLog
public abstract class AbstractEntity extends AbstractObject {

	// Serialisation identifier -----------------------------------------------

	private static final long	serialVersionUID	= 1L;

	// Attributes -------------------------------------------------------------

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private int					id;

	@Version
	private int					version;

	// Properties -------------------------------------------------------------


	@Transient
	public boolean isTransient() {
		boolean result;

		result = this.id == 0;

		return result;
	}

	// Object interface -------------------------------------------------------

	@Override
	public int hashCode() {
		int result;

		result = this.getId();
		if (this.getId() == 0)
			AbstractEntity.logger.warn("Hashing transient entity {}!", this);

		return result;
	}

	@Override
	public boolean equals(final Object other) {
		boolean result;
		AbstractEntity that;

		if (this == other)
			result = true;
		else if (other == null)
			result = false;
		else if (other instanceof Integer) {
			AbstractEntity.logger.warn("Comparing entity {} to integer {}!", this, other);
			result = this.getId() == (Integer) other;
		} else if (!this.getClass().isInstance(other))
			result = false;
		else {
			that = (AbstractEntity) other;
			if (this.isTransient() && !that.isTransient() || !this.isTransient() && that.isTransient())
				result = false;
			else if (!this.isTransient() && !that.isTransient())
				result = this.getId() == that.getId();
			else {
				AbstractEntity.logger.warn("Comparing transient entities {} and {} reflectively!", this, that);
				result = EqualsBuilder.reflectionEquals(this, that);
			}
		}

		return result;
	}

	@Override
	public String toString() {
		StringBuilder result;

		result = new StringBuilder();
		result.append(this.getClass().getName());
		result.append("{");
		result.append("id=");
		result.append(this.getId());
		result.append(", version=");
		result.append(this.getVersion());
		result.append("}");

		return result.toString();
	}

}
