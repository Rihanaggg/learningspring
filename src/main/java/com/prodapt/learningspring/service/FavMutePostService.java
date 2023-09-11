package com.prodapt.learningspring.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.prodapt.learningspring.entity.Favpost;
import com.prodapt.learningspring.entity.Mutedpost;
import com.prodapt.learningspring.entity.Post;
import com.prodapt.learningspring.entity.User;
import com.prodapt.learningspring.repository.FavPostRepository;
import com.prodapt.learningspring.repository.MutedPostRepository;

import jakarta.annotation.PostConstruct;

@Service
public class FavMutePostService {

	private final FavPostRepository favPostRepository;
	private final MutedPostRepository mutedPostRepository;

 
	
	public List<Favpost> favpostList;
	
	public List<Mutedpost> mutedpostList;

	@PostConstruct
	public void init() {
		
		favpostList = new ArrayList<>();
		
		mutedpostList = new ArrayList<>();
	}

	@Autowired
	public FavMutePostService(FavPostRepository favPostRepository, MutedPostRepository mutedPostRepository) {
		this.favPostRepository = favPostRepository;
		this.mutedPostRepository = mutedPostRepository;
	}

	public List<Favpost> findAllFavPostsByUser(User user) {
		return favPostRepository.findAllByUser(user);
	}

	public boolean isFavPost(User user, Post post) {
		return favPostRepository.existsByUserAndPost(user, post);
	}

//	public Favpost addFavPost(User user, Post post) {
//		if (!isFavPost(user, post)) {
//			Favpost favpost = new Favpost(user, post);
//			return favPostRepository.save(favpost);
//		}
//		return null; // Return null to indicate that the post is already a favorite.
//	}

	public void removeFavPost(User user, Post post) {
		Favpost favpost = favPostRepository.findByUserAndPost(user, post);
		if (favpost != null) {
			favPostRepository.delete(favpost);
		}
	}

	public List<Mutedpost> findAllMutedPostsByPostId(Integer postId) {
		return mutedPostRepository.findAllByPostId(postId);
	}

	public boolean isPostMutedByUser(User user, Post post) {
		return mutedPostRepository.existsByUserAndPost(user, post);
	}

	public List<Post> findAllPostsNotMutedByUser(User user) {
		return mutedPostRepository.findAllPostsNotMutedByUser(user);
	}

//	public Mutedpost mutePost(User user, Post post) {
//		if (!isPostMutedByUser(user, post)) {
//			Mutedpost mutedpost = new Mutedpost(user, post);
//			return mutedPostRepository.save(mutedpost);
//		}
//		return null; // Return null to indicate that the post is already muted.
//	}

	public void unmutePost(User user, Post post) {
		Mutedpost mutedpost = mutedPostRepository.findByUserAndPost(user, post);
		if (mutedpost != null) {
			mutedPostRepository.delete(mutedpost);
		}
	}
}
