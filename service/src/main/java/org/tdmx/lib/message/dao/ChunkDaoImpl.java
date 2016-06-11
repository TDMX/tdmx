/*
 * TDMX - Trusted Domain Messaging eXchange
 * 
 * Enterprise B2B messaging between separate corporations via interoperable cloud service providers.
 * 
 * Copyright (C) 2014 Peter Klauser (http://tdmx.org)
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses/.
 */
package org.tdmx.lib.message.dao;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import javax.sql.DataSource;

import org.tdmx.lib.message.domain.Chunk;

public class ChunkDaoImpl implements ChunkDao {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	private DataSource dataSource;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// TODO #107: split into 0..9-a..f sub-tables.

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void store(Chunk chunk) throws SQLException {
		try {
			insertChunk(chunk);
		} catch (SQLException e) {
			// in the case of constrain violation ( resend ) - delete existing and add our chunk
			try {
				deleteChunk(chunk);
			} catch (SQLException de) {
				// ignore
			}
			insertChunk(chunk);
		}
	}

	@Override
	public Chunk loadByMsgIdAndPos(String msgId, int pos) throws SQLException {
		return selectChunk(msgId, pos);
	}

	@Override
	public void deleteByMsgId(String msgId) throws SQLException {
		deleteMessage(msgId);
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private void insertChunk(Chunk chunk) throws SQLException {
		try (Connection con = dataSource.getConnection()) {
			String msgId = chunk.getMsgId();
			String sql = getInsertSql(msgId);
			try (PreparedStatement statement = con.prepareStatement(sql)) {
				statement.setString(1, msgId);
				statement.setInt(2, chunk.getPos());
				statement.setString(3, chunk.getMac());
				statement.setTimestamp(4, new Timestamp(chunk.getTtlTimestamp().getTime()));
				Blob dataBlob = con.createBlob();
				dataBlob.setBytes(1, chunk.getData());
				statement.setBlob(5, dataBlob);

				statement.executeUpdate();
			}
		}
	}

	private Chunk selectChunk(String msgId, int pos) throws SQLException {
		Chunk result = new Chunk();
		try (Connection con = dataSource.getConnection()) {
			String sql = getSelectSql(msgId);
			try (PreparedStatement statement = con.prepareStatement(sql)) {
				statement.setString(1, msgId);
				statement.setInt(2, pos);

				try (ResultSet rs = statement.executeQuery()) {
					if (rs.next()) {
						result.setMsgId(rs.getString(1));
						result.setPos(rs.getInt(2));
						result.setMac(rs.getString(3));
						result.setTtlTimestamp(new Date(rs.getTimestamp(4).getTime()));
						Blob dataBlob = rs.getBlob(5);
						result.setData(dataBlob.getBytes(1, (int) dataBlob.length()));
					}
				}
			}
		}
		if (result.getMsgId() != null && result.getData() != null) {
			return result;
		}
		return null;
	}

	private void deleteChunk(Chunk chunk) throws SQLException {
		try (Connection con = dataSource.getConnection()) {
			String msgId = chunk.getMsgId();
			String sql = getDeleteChunkSql(msgId);
			try (PreparedStatement statement = con.prepareStatement(sql)) {
				statement.setString(1, msgId);
				statement.setInt(2, chunk.getPos());
				statement.executeUpdate();
			}
		}
	}

	private void deleteMessage(String msgId) throws SQLException {
		try (Connection con = dataSource.getConnection()) {
			String sql = getDeleteMessageSql(msgId);
			try (PreparedStatement statement = con.prepareStatement(sql)) {
				statement.setString(1, msgId);
				statement.executeUpdate();
			}
		}
	}

	private String getInsertSql(String msgId) {
		return "INSERT INTO chunk_" + getTableNr(msgId) + " (msgId,pos,mac,ttl,data) values (?,?,?,?,?)";
	}

	private String getSelectSql(String msgId) {
		return "SELECT msgId, pos, mac, ttl, data FROM chunk_" + getTableNr(msgId) + " WHERE msgId = ? and pos = ?";
	}

	private String getDeleteChunkSql(String msgId) {
		return "DELETE FROM chunk_" + getTableNr(msgId) + " where msgId = ? and pos = ?";
	}

	private String getDeleteMessageSql(String msgId) {
		return "DELETE FROM chunk_" + getTableNr(msgId) + " where msgId = ?";
	}

	private String getTableNr(String msgId) {
		return "" + msgId.charAt(4);
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

}
