package com.nuc.iblog.service.imp;


import com.nuc.iblog.entity.Article;
import com.nuc.iblog.entity.ArticlePage;
import com.nuc.iblog.entity.Category;
import com.nuc.iblog.entity.User;
import com.nuc.iblog.jpa.ArticleJpa;
import com.nuc.iblog.jpa.CategoryJpa;
import com.nuc.iblog.jpa.CommentsJpa;
import com.nuc.iblog.jpa.UserJpa;
import com.nuc.iblog.service.ClArticleService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Tyranitarx on 2018/1/11.
 *
 * @Description :
 */
@Service
public class ClClArticleServiceImp implements ClArticleService {
    Logger log = org.slf4j.LoggerFactory.getLogger(ClClArticleServiceImp.class);
    @Autowired
    private ArticleJpa articleJpa;
    @Autowired
    private UserJpa userJpa;
    private Article article;
    private List<Article> articles;

    /**
     * 通过aid获取相应的文章
     *
     * @param aid
     * @return
     */
    @Override
    public Article getArticle(int aid) {
        article = articleJpa.findByAid(aid);
        return article;
    }

    private int num;
    private List<Article> returnArticle;

    /**
     * 获取首页文章
     *
     * @return
     */
    @Override
    public List<Article> getArticles() {
        returnArticle=new ArrayList<Article>();
        for(int i=36;i<=40;i++) {
            article = articleJpa.findByAid(i);
            article.setContent(article.getContent().replaceAll("\n",""));
            returnArticle.add(article);
        }
        return returnArticle;
    }


    private User user;

    /**
     * 获取用户个人文章
     *
     * @param uid
     * @return
     */
    @Override
    public List<Article> getArticlesByUser(int uid) {
        user = userJpa.findByUid(uid);
        return user.getArticles();
    }

    @Override
    public List<Article> getArticlesByCategory(int catid) {
        returnArticle = new ArrayList<Article>();
        articles = articleJpa.findArticleByAcademy(catid);
        log.info(articles.size()+"");
        if (articles.size() != 0&&articles.size()>5) {
            for (int i = articles.size() - 1; i >= articles.size() - 5; i--) {
                article = articles.get(i);
                returnArticle.add(article);
            }
        }
        else {
            Iterator<Article> iterator=articles.iterator();
            while (iterator.hasNext()){
                article=iterator.next();
                returnArticle.add(article);
            }
        }
        return returnArticle;
    }

    private Map<Category, List<Article>> academyArticleMap;
    private List<Category> categoryList;
    private Category category;
    private List<Article> articleList;

    @Override
    public Map<Category, List<Article>> getAllAcademyArticle() {
        academyArticleMap = new LinkedHashMap<Category, List<Article>>();
        categoryList = categoryJpa.findCategoryByCategoryEqualsOrderByCatidAsc(null);
        Iterator<Category> categoryIterator = categoryList.iterator();
        while (categoryIterator.hasNext()) {
            category = categoryIterator.next();
            articleList = articleJpa.findArticleByAcademy(category.getCatid());
            academyArticleMap.put(category, articleList);
        }
        return academyArticleMap;
    }

    private ArticlePage articlePage;

    @Override
    public ArticlePage getAcademyArticle(int catid, int pagenum) {
        int page = 10;
        int totalpage;
            articlePage = new ArticlePage();
            articleList = articleJpa.findArticleByAcademy(catid);
            int lastpage = articleList.size() % page;
        if (articleList.size()!=0) {
            if (lastpage == 0)
                totalpage = articleList.size() / page;
            else
                totalpage = articleList.size() / page + 1;
            if (pagenum + 1 == totalpage) {
                articlePage.setContent(articleList.subList(pagenum * 10, articleList.size()));
                articlePage.setTotalpage(totalpage);
                return articlePage;
            } else {
                articlePage.setContent(articleList.subList(pagenum * 10, (pagenum + 1) * 10));
                articlePage.setTotalpage(totalpage);
                return articlePage;
            }
        }else {
            articlePage.setContent(articleList);
            articlePage.setTotalpage(0);
            return articlePage;
            }
        }

    @Override
    public Page<Article> getArticleByUserAndCategory(int uid, int catid,int pagenum) {
        Pageable pageable=new PageRequest(pagenum,3);
        return articleJpa.findByUserAndCategoryOrderByAidDesc(userJpa.findByUid(uid), categoryJpa.findByCatid(catid),pageable);
    }

    @Autowired
    private CategoryJpa categoryJpa;

    /**
     * 用户添加文章
     *
     * @param uid
     * @param content
     * @return
     */
    @Override
    public int insertArticle(int uid, String catname, String title, String summary, String content) {
        SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        article = new Article();
        log.info("获取到的分类" + catname + categoryJpa.findCategoryByName(catname));
        article.setCategory(categoryJpa.findCategoryByName(catname));
        article.setUser(userJpa.findByUid(uid));
        article.setDate(sdf.format(new Date()));
        article.setContent(content);
        article.setSummary(summary);
        article.setTitle(title);
        articleJpa.save(article);
        return 1;
    }

    @Override
    public int updateArticle(int uid, int aid, String catname, String title, String summary, String content) {
        SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        article = articleJpa.findByAid(aid);
        article.setCategory(categoryJpa.findCategoryByName(catname));
        article.setUser(userJpa.findByUid(uid));
        article.setDate(sdf.format(new Date()));
        article.setContent(content);
        article.setSummary(summary);
        article.setTitle(title);
        articleJpa.save(article);
        return 1;
    }

    @Autowired
    private CommentsJpa commentsJpa;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteArticle(int aid) {
        commentsJpa.deleteCommentsByArticle(articleJpa.findByAid(aid));
        return articleJpa.deleteArticleByAid(aid);
    }

    @Override
    public Page<Article> getPageArticle(int uid, int pagenum) {
        Pageable pageable = new PageRequest(pagenum, 3);
        return articleJpa.findByUserOrderByAidDesc(userJpa.findByUid(uid), pageable);
    }

}
