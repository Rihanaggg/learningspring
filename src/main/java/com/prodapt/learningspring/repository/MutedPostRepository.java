package com.prodapt.learningspring.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.prodapt.learningspring.entity.Mutedpost;
import com.prodapt.learningspring.entity.Post;
import com.prodapt.learningspring.entity.User;

public interface MutedPostRepository extends CrudRepository<Mutedpost, Integer> {
	@Query(value = "select * from Mutedpost c where post_id = ?1", nativeQuery = true)

	List<Mutedpost> findAllByPostId(Integer postId);

	boolean existsByUserAndPost(User user, Post post);

	List<Mutedpost> findAllByUser(User user);

	@Query("SELECT p FROM Post p WHERE p NOT IN (SELECT mp.post FROM Mutedpost mp WHERE mp.user = :user)")
	List<Post> findAllPostsNotMutedByUser(User user);

	Mutedpost findByUserAndPost(User user, Post post);
}
