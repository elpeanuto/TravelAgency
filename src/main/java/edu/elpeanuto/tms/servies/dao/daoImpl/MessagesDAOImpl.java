package edu.elpeanuto.tms.servies.dao.daoImpl;

import edu.elpeanuto.tms.model.Message;
import edu.elpeanuto.tms.servies.dao.MessagesDAO;
import edu.elpeanuto.tms.servies.dao.db.DBConnection;
import edu.elpeanuto.tms.servies.dao.db.PoolConnectionBuilder;
import edu.elpeanuto.tms.servies.dto.MessageDTO;
import edu.elpeanuto.tms.servies.exception.DAOException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Class that communicate with database (message, messageAnswer tables)
 * @see edu.elpeanuto.tms.servies.dao.BaseDAO
 * @see edu.elpeanuto.tms.servies.dao.MessagesDAO
 */
public class MessagesDAOImpl implements MessagesDAO {
    private final DBConnection dbConnection;

    public MessagesDAOImpl() {
        dbConnection = PoolConnectionBuilder.getInstance();
    }

    public MessagesDAOImpl(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dbConnection.getConnection();
    }

    @Override
    public Optional<Message> get(Long id) throws DAOException {
        String getPattern = "SELECT * FROM messages INNER JOIN message_answer ON" +
                " messages.message_answer_id = message_answer.id  WHERE messages.id=?";

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(getPattern)
        ) {
            stmt.setLong(1, id);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(setMessage(rs));
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new DAOException(String.format("SQLException in get(Long id), params: %s" , id.toString()), e);
        }
    }

    @Override
    public List<Message> getAll() throws DAOException {
        String getAllPattern = "SELECT * FROM messages INNER JOIN message_answer ON" +
                                " messages.message_answer_id = message_answer.id";

        List<Message> messageList = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(getAllPattern)
        ) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                messageList.add(setMessage(rs));
            }

            return messageList;
        } catch (SQLException e) {
            throw new DAOException("SQLException in getAll()");
        }
    }

    @Override
    public boolean save(Message message) throws DAOException {
        String messageSavePattern = "INSERT INTO messages(message_answer_id, user_id, user_email, user_name, category, message, received_date)" +
                " values(?,?,?,?,?,?,?)";
        String messageAnswerSavePattern = "INSERT INTO message_answer(admin_id, answer, processing_date) values(?,?,?)";

        try (Connection con = getConnection()) {
            try (PreparedStatement stmt1 = con.prepareStatement(messageSavePattern);
                 PreparedStatement stmt2 = con.prepareStatement(messageAnswerSavePattern, Statement.RETURN_GENERATED_KEYS)) {
                con.setAutoCommit(false);

                stmt2.setLong(1, message.getAdminId() == null ? 0 : message.getAdminId());
                stmt2.setString(2, message.getAnswer());
                stmt2.setString(3, message.getProcessingDate());

                int rowCounter1 = stmt2.executeUpdate();

                try (ResultSet generatedKeys = stmt2.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        stmt1.setLong(1, generatedKeys.getLong(1));
                        stmt1.setLong(2, message.getUserId());
                        stmt1.setString(3, message.getUserEmail());
                        stmt1.setString(4, message.getUserName());
                        stmt1.setString(5, message.getCategory());
                        stmt1.setString(6, message.getMessage());
                        stmt1.setString(7, message.getReceivedDate());
                    } else {
                        logger.error("Fatal error in save(Message message), no ID obtained.");
                        throw new RuntimeException();
                    }
                }

                int rowCounter2 = stmt1.executeUpdate();

                con.commit();

                return rowCounter1 > 0 && rowCounter2 > 0;
            } catch (SQLException e) {
                con.rollback();
                throw new SQLException(e);
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DAOException(String.format("SQLException in save(Message message), params: %s" , message.toString()), e);
        }
    }

    @Override
    public boolean update(Message message) throws DAOException {
        String messageSavePattern = "UPDATE messages SET user_id=?, user_email=?, category=?, message=?, received_date=? WHERE id=?";

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(messageSavePattern)
        ) {
            stmt.setLong(1, message.getUserId());
            stmt.setString(2, message.getUserEmail());
            stmt.setString(3, message.getCategory());
            stmt.setString(4, message.getMessage());
            stmt.setString(5, message.getReceivedDate());
            stmt.setLong(6, message.getId());

            int rowCounter = stmt.executeUpdate();

            return rowCounter > 0;
        } catch (SQLException e) {
            throw new DAOException(String.format("SQLException in update(Message message), params: %s" , message.toString()), e);
        }
    }

    @Override
    public boolean adminUpdate(MessageDTO messageDTO) throws DAOException {
        String savePattern = "UPDATE message_answer SET admin_id=?, answer=?, processing_date=? WHERE id=?";

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(savePattern)
        ) {
            stmt.setLong(1, messageDTO.getAdminId());
            stmt.setString(2, messageDTO.getResponse());
            stmt.setString(3, messageDTO.getProcessingDate());
            stmt.setLong(4, messageDTO.getMessageId());

            int rowCounter = stmt.executeUpdate();

            return rowCounter > 0;
        } catch (SQLException e) {
            throw new DAOException(String.format("SQLException in adminUpdate(MessageDTO messageDTO), params: %s" , messageDTO.toString()), e);
        }
    }

    @Override
    public List<Message> getPaginationByUserId(Integer start, Integer numOfStrings, Long userId) throws DAOException {
        String getAllPattern = "SELECT * FROM messages JOIN message_answer ma ON messages.message_answer_id = ma.id WHERE user_id = ? LIMIT ?, ?";

        List<Message> messageList = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(getAllPattern)
        ) {
            stmt.setLong(1, userId);
            stmt.setInt(2, start);
            stmt.setInt(3, numOfStrings);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                messageList.add(setMessage(rs));
            }

            return messageList;
        } catch (SQLException e) {
            throw new DAOException(String.format("SQLException in getPaginationByUserId(Integer start, Integer numOfStrings, Long userId), params: start: %s, numOfStrings: %s, userId: %s"
                    , start.toString(), numOfStrings, userId.toString()), e);
        }
    }

    @Override
    public Optional<Integer> getNumberOfNotesByUserId(Long userId) throws DAOException {
        String numRegisteredOrders = "SELECT COUNT(*) FROM messages JOIN message_answer ma on messages.message_answer_id = ma.id WHERE user_id = ?";

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(numRegisteredOrders)
        ) {
            stmt.setLong(1, userId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(rs.getInt(1));
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new DAOException(String.format("SQLException in getNumberOfNotesByUserId(Long userId), params: %s" , userId.toString()), e);
        }
    }

    @Override
    public List<Message> getPaginationNotAnswered(Integer start, Integer numOfStrings) throws DAOException {
        String getAllPattern = "SELECT * FROM messages JOIN message_answer ma on messages.message_answer_id = ma.id WHERE ma.processing_date IS NULL LIMIT ?, ?;";

        List<Message> messageList = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(getAllPattern)
        ) {
            stmt.setInt(1, start);
            stmt.setInt(2, numOfStrings);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                messageList.add(setMessage(rs));
            }

            return messageList;
        } catch (SQLException e) {
            throw new DAOException(String.format("SQLException in getPaginationNotAnswered(Integer start, Integer numOfStrings), params: start: %s, numOfStrings: %s"
                    , start.toString(), numOfStrings), e);
        }
    }

    @Override
    public Optional<Integer> getNumberOfNotesNotAnswered() throws DAOException {
        String numRegisteredOrders = "SELECT COUNT(*) FROM messages JOIN message_answer ma on messages.message_answer_id = ma.id WHERE ma.processing_date IS NULL";

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(numRegisteredOrders)
        ) {
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(rs.getInt(1));
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new DAOException("SQLException in getNumberOfNotesNotAnswered())", e);
        }
    }

    private Message setMessage(ResultSet rs) throws SQLException {
        return new Message(rs.getLong("id"), rs.getLong("message_answer_id"), rs.getLong("user_id"), rs.getLong("admin_id"),
                rs.getString("user_email"), rs.getString("user_name"), rs.getString("category"), rs.getString("message"),
                rs.getString("answer"), rs.getString("received_date"), rs.getString("processing_date"));
    }

    @Override
    public Optional<Integer> getNumberOfNotes() throws DAOException {
        return getNumberOfNotesNotAnswered();
    }

    @Override
    public List<Message> getPagination(Integer start, Integer numOfStrings) throws DAOException {
        return getPaginationNotAnswered(start, numOfStrings);
    }
}
