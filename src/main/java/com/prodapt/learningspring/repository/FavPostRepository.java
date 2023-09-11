package com.prodapt.learningspring.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.prodapt.learningspring.entity.Favpost;
import com.prodapt.learningspring.entity.Post;
import com.prodapt.learningspring.entity.User;

public interface FavPostRepository extends CrudRepository<Favpost, Integer> {

    List<Favpost> findAllByUser(User user);
    boolean existsByUserAndPost(User user, Post post);
    
    Favpost findByUserAndPost(User user, Post post);
    
    void deleteByUserAndPost(User user, Post post);
    
}
