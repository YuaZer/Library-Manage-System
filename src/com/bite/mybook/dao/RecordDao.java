package com.bite.mybook.dao;

import com.bite.mybook.bean.Record;
import com.bite.mybook.biz.BookBiz;
import com.bite.mybook.biz.MemberBiz;
import com.bite.mybook.util.DBHelper;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class RecordDao {
    QueryRunner runner = new QueryRunner();
    MemberBiz memberBiz = new MemberBiz();
    BookBiz bookBiz = new BookBiz();

    private static final String BASE_COLUMNS = "r.*, case when r.backDate is null then 0 else 1 end as isBack";

    // 查询全部借阅记录
    public List<Record> getAll() throws SQLException {
        Connection connection = DBHelper.getConnection();

        String sql = "select " + BASE_COLUMNS + " from record r";

        List<Record> records = runner.query(connection, sql, new BeanListHandler<>(Record.class));

        connection.close();

        return records;
    }

    // 查询已归还记录
    public List<Record> getReturned() throws SQLException {
        Connection connection = DBHelper.getConnection();
        String sql = "select " + BASE_COLUMNS + " from record r where r.backDate is not null";
        List<Record> records = runner.query(connection, sql, new BeanListHandler<>(Record.class));
        connection.close();
        return records;
    }

    // 查询未归还记录
    public List<Record> getNotReturn() throws SQLException {
        Connection connection = DBHelper.getConnection();
        String sql = "select " + BASE_COLUMNS + " from record r where r.backDate is null";
        List<Record> records = runner.query(connection, sql, new BeanListHandler<>(Record.class));
        connection.close();
        return records;
    }

    // 增加用户借阅记录
    public boolean add(long memberId, long bookId, double deposit, long userId, String isbn) throws SQLException {
        Connection connection = DBHelper.getConnection();
        String sql = "insert into record (memberId, bookId, rentDate, backDate, deposit, userId, isbn) values (?,?,CURRENT_DATE,null,?,?,?)";

        int line = runner.update(connection, sql, memberId,bookId, deposit, userId, isbn);

        connection.close();
        return line == 1;
    }

    // 通过用户 id 查询借阅记录
    public List<Record> getRecordById(long id) throws SQLException {
        Connection connection = DBHelper.getConnection();

        String sql = "select " + BASE_COLUMNS + " from record r where r.memberId=?";

        List<Record> records = runner.query(connection, sql, new BeanListHandler<>(Record.class), id);

        connection.close();

        return records;
    }

    public Record getRecordByRecordId(long recordId) throws SQLException {
        Connection connection = DBHelper.getConnection();
        String sql = "select " + BASE_COLUMNS + " from record r where r.id=?";
        Record record = runner.query(connection, sql, new BeanHandler<>(Record.class), recordId);
        connection.close();
        return record;
    }

    // 归还书籍
    public boolean backBooks(long mid, long id) throws SQLException {
        Connection connection = DBHelper.getConnection();

        String sql = "update record set backDate=CURRENT_DATE where memberId=? and id=?";
        int updated = runner.update(connection, sql, mid, id);

        boolean success = updated > 0;
        if (success) {
            String sql2 = "select " + BASE_COLUMNS + " from record r where r.id=?";
            Record record = runner.query(connection, sql2, new BeanHandler<>(Record.class), id);
            if (record != null) {
                double deposit = record.getDeposit();
                boolean refund = memberBiz.memberChangeByMid(record.getMemberId(), deposit);
                boolean stockBack = bookBiz.modify(record.getBookId(), 1);
                success = refund && stockBack;
            } else {
                success = false;
            }
        }
        connection.close();
        return success;
    }


    public static void main(String[] args) {
        RecordDao recordDao = new RecordDao();
        List<Record> all;
        try {
            all = recordDao.getReturned();
            System.out.println(all);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
