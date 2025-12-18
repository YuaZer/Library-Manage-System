package com.bite.mybook.biz;

import com.bite.mybook.bean.Book;
import com.bite.mybook.bean.Member;
import com.bite.mybook.bean.Record;
import com.bite.mybook.dao.RecordDao;
import com.bite.mybook.util.DBHelper;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class RecordBiz {
    RecordDao recordDao = new RecordDao();
    BookBiz bookBiz = new BookBiz();
    MemberBiz memberBiz = new MemberBiz();
    UserBiz userBiz = new UserBiz();


//    // 查询全部借阅记录
//    public List<Record> getAll(){
//
//    }
//
//    // 查询已归还记录
//    public List<Record> getReturned(){
//
//    }
//
//    // 查询未归还记录
//    public List<Record> getNotReturn(){
//
//    }
//
//    // 查询最近一周需归还
//    public List<Record> getLastWeekNeedReturn(){
//
//    }

    // 增加用户借阅书记录
    public boolean add(long mid,long[] bookIds,long userId){
        boolean ret = true;
        try{
            for (long bookid : bookIds) {
                // 书籍对象
                Book book = bookBiz.getBookByBookId(bookid);

                // 书籍押金
                double deposit = book.getPrice() * 0.3f;
                // 修改用户余额
                memberBiz.memberChangeByMid(mid,0-deposit);

                // 修改库存
                bookBiz.modify(book.getId(),-1);

                String isbn = String.format("BOOK-%05d", book.getId());
                boolean add = recordDao.add(mid, bookid, deposit, userId, isbn);
                if (!add){
                    ret = false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }

    // 通过用户 id 查询借阅记录
    public List<Record> getRecordById(long id){
        try {
            return recordDao.getRecordById(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 通过用户 idNumber 查询借阅记录
    public List<Record> getRecordByIdNumber(String idNumber){
        List<Record> records = null;
        try {
            long memberId = memberBiz.getIdByIdNum(idNumber);
            Member member = null;
            if (memberId >= 0) {
                member = memberBiz.getById(memberId);
            } else {
                try {
                    long parsed = Long.parseLong(idNumber);
                    member = memberBiz.getById(parsed);
                    if (member != null) {
                        memberId = member.getId();
                    }
                } catch (NumberFormatException ignore) {
                }
            }
            if (member == null || memberId < 0) {
                return null;
            }

            records = recordDao.getRecordById(memberId);
            if (records == null) {
                records = new LinkedList<>();
            }

            for (Record record : records) {
                long bookId = record.getBookId();
                long userId = record.getUserId();
                record.setBook(bookBiz.getBookByBookId(bookId));
                record.setMember(member);
                record.setUser(userBiz.getUserById(userId));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    public boolean backBooks(long mid, String[] split) {
        boolean backAll = true;
        try {
            long[] Ids = new long[split.length];
            for (int i = 0; i < split.length; i++) {
                Ids[i] = Long.parseLong(split[i]);
            }
            for (long id : Ids) {
                backAll = recordDao.backBooks(mid,id) && backAll;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return backAll;
    }

    // 查询全部借阅记录
    public List<Record> getAll()  {
        List<Record> records = null;
        try {
            records = recordDao.getAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return records;
    }

    // 查询已归还记录
    public List<Record> getReturned()  {
        List<Record> records = null;
        try {
            records = recordDao.getReturned();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    // 查询未归还记录
    public List<Record> getNotReturn() {
        List<Record> records = null;
        try {
            records = recordDao.getNotReturn();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    public Record getRecordByRecordId(long recordId){
        try {
            return recordDao.getRecordByRecordId(recordId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
