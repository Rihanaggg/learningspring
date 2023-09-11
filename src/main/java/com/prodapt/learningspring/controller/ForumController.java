package com.prodapt.learningspring.controller;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.prodapt.learningspring.controller.binding.AddPostForm;
import com.prodapt.learningspring.controller.exception.ResourceNotFoundException;
import com.prodapt.learningspring.dto.PostDTO;
import com.prodapt.learningspring.entity.Comment;
import com.prodapt.learningspring.entity.Favpost;
import com.prodapt.learningspring.entity.LikeId;
import com.prodapt.learningspring.entity.LikeRecord;
import com.prodapt.learningspring.entity.Mutedpost;
import com.prodapt.learningspring.entity.Post;
import com.prodapt.learningspring.entity.User;
import com.prodapt.learningspring.model.RegistrationForm;
import com.prodapt.learningspring.repository.CommentRepository;
import com.prodapt.learningspring.repository.FavPostRepository;
import com.prodapt.learningspring.repository.LikeCRUDRepository;
import com.prodapt.learningspring.repository.MutedPostRepository;
import com.prodapt.learningspring.repository.PostRepository;
import com.prodapt.learningspring.repository.UserRepository;
import com.prodapt.learningspring.service.CommentService;
import com.prodapt.learningspring.service.DomainUserService;
import com.prodapt.learningspring.service.FavMutePostService;
import com.prodapt.learningspring.service.PostService;

import jakarta.servlet.ServletException;

@Controller
@RequestMapping("/forum")
public class ForumController {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private DomainUserService domainUserService;

  @Autowired
  private LikeCRUDRepository likeCRUDRepository;

  @Autowired
  private PostService postService;

  @Autowired
  private PostRepository postRepository;

  @Autowired
  private CommentService commentService;

  @Autowired
  private FavMutePostService favMutePostService;

  @Autowired
  private CommentRepository commentRepository;

  @Autowired
  private MutedPostRepository mutedPostRepository;

  @Autowired
  private FavPostRepository favPostRepository;

  @GetMapping("/post/form")
  public String getPostForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
    AddPostForm postForm = new AddPostForm();
    User author = domainUserService.getByName(userDetails.getUsername()).get();
    postForm.setUserId(author.getId());
    model.addAttribute("postForm", postForm);
    return "forum/postForm";
  }

  @GetMapping("/post/all")
  public String getallPosts(Model model, Principal principal,@AuthenticationPrincipal UserDetails userDetails) {
    Optional<User> user = userRepository.findByName(userDetails.getUsername());
    List<Post> primposts = mutedPostRepository.findAllPostsNotMutedByUser(user.get());
    List<PostDTO> posts = postService.convert(primposts);
    model.addAttribute("isLoggedIn", principal != null);
    if (principal != null) {
      model.addAttribute("username", principal.getName());
    }
    model.addAttribute("posts", posts);
    return "forum/allPosts";
  }

  @PostMapping("/post/add")
  public String addNewPost(@ModelAttribute("postForm") AddPostForm postForm, BindingResult bindingResult,
      RedirectAttributes attr) throws ServletException {
    if (bindingResult.hasErrors()) {
      System.out.println(bindingResult.getFieldErrors());
      attr.addFlashAttribute("org.springframework.validation.BindingResult.post", bindingResult);
      attr.addFlashAttribute("post", postForm);
      return "redirect:/forum/post/form";
    }
    Optional<User> user = userRepository.findById(postForm.getUserId());
    if (user.isEmpty()) {
      throw new ServletException("Something went seriously wrong and we couldn't find the user in the DB");
    }
    Post post = new Post();
    post.setAuthor(user.get());
    post.setTitle(postForm.getTitle());
    post.setContent(postForm.getContent());
    postRepository.save(post);

    return String.format("redirect:/forum/post/%d", post.getId());
  }

  @GetMapping("/post/{id}")
  public String postDetail(@PathVariable int id, Model model, Principal principal) throws ResourceNotFoundException {
    model.addAttribute("isLoggedIn", principal != null);
    if (principal != null) {
      model.addAttribute("username", principal.getName());
    }
    PostDTO post = postService.findById(id);
    model.addAttribute("post", post);
    model.addAttribute("comments", commentService.findAllByPostId(id));
    return "forum/postDetail";
  }

  @PostMapping("/post/{id}/like")
  public String postLike(@PathVariable int id, RedirectAttributes attr,
      @AuthenticationPrincipal UserDetails userDetails) {
    LikeId likeId = new LikeId();
    User user = domainUserService.getByName(userDetails.getUsername()).get();
    likeId.setUser(userRepository.findByName(user.getName()).get());
    likeId.setPost(postRepository.findById(id).get());
    LikeRecord like = new LikeRecord();
    like.setLikeId(likeId);
    likeCRUDRepository.save(like);
    return "redirect:/forum/post/all";
  }

  @PostMapping("/post/{id}/comment")
  public String addComment(@PathVariable int id, @RequestParam String content,
      @AuthenticationPrincipal UserDetails userDetails) {
    Comment comment = new Comment();
    comment.setUser(domainUserService.getByName(userDetails.getUsername()).get());
    comment.setPost(postRepository.findById(id).get());
    comment.setContent(content);
    commentRepository.save(comment);
    return String.format("redirect:/forum/post/%d", id);
  }

  @PostMapping("/post/{id}/reply/{parentId}")
  public String addComment(@PathVariable int id, @PathVariable int parentId, @RequestParam String content,
      @AuthenticationPrincipal UserDetails userDetails) {
    Comment comment = new Comment();
    comment.setUser(domainUserService.getByName(userDetails.getUsername()).get());
    comment.setPost(postRepository.findById(id).get());
    comment.setParent(commentRepository.findById(parentId).get());
    comment.setContent(content);
    commentRepository.save(comment);
    return String.format("redirect:/forum/post/%d", id);
  }

  @GetMapping("/register")
  public String getRegistrationForm(Model model) {
    if (!model.containsAttribute("registrationForm")) {
      model.addAttribute("registrationForm", new RegistrationForm());
    }
    return "forum/register";
  }

  @PostMapping("/register")
  public String register(@ModelAttribute("registrationForm") RegistrationForm registrationForm,
      BindingResult bindingResult,
      RedirectAttributes attr) {
    if (bindingResult.hasErrors()) {
      attr.addFlashAttribute("org.springframework.validation.BindingResult.registrationForm", bindingResult);
      attr.addFlashAttribute("registrationForm", registrationForm);
      return "redirect:/register";
    }
    if (!registrationForm.isValid()) {
      attr.addFlashAttribute("message", "Passwords must match");
      attr.addFlashAttribute("registrationForm", registrationForm);
      return "redirect:/register";
    }
    System.out.println(domainUserService.save(registrationForm.getUsername(), registrationForm.getPassword()));
    attr.addFlashAttribute("result", "Registration success!");
    return "redirect:/login";
  }

  // adding post to favorite

  @PostMapping("/post/{id}/fav")
  public String AddfavPost(@PathVariable int id,@AuthenticationPrincipal UserDetails userDetails,RedirectAttributes redirectAttributes) {
    Optional<User> user = userRepository.findByName(userDetails.getUsername());
    Optional<Post> post = postRepository.findById(id);
    System.out.println(id);
    System.out.println(user);
    if (user.isPresent() && post.isPresent()) {

      // check if the user and post is there in the opposite table
      if (mutedPostRepository.existsByUserAndPost(user.get(), post.get())) {
        return "redirect:/forum/post/all";
      }

      if (favPostRepository.existsByUserAndPost(user.get(), post.get())) {
        return "redirect:/forum/post/all";
      } else {
        Favpost favPost = new Favpost();
        favPost.setPost(post.get());
        favPost.setUser(user.get());

        favPostRepository.save(favPost);
        redirectAttributes.addFlashAttribute("FavMessage", "post added to favorites");

        return "redirect:/forum/post/all";

      }
    }
    return "redirect:/forum/post/error";
  }

  // Muting post

  @PostMapping("/post/{id}/mute")

  public String AddmutedPost(@PathVariable int id,@AuthenticationPrincipal UserDetails userDetails,RedirectAttributes redirectAttributes) {
    Optional<User> user = userRepository.findByName(userDetails.getUsername());
    Optional<Post> post = postRepository.findById(id);

    if (user.isPresent() && post.isPresent()) {
      User commentAuthor = post.get().getAuthor();
      // checking if the user and post is there in the opposite table

      if (favPostRepository.existsByUserAndPost(user.get(), post.get())) {
        return "redirect:/forum/post/all";
      }

      if (mutedPostRepository.existsByUserAndPost(user.get(), post.get())) {
        return "redirect:/forum/post/all";

      }

      if (user.get().equals(commentAuthor)) {
        return "redirect:/forum/post/all";
      }
      Mutedpost mutePost = new Mutedpost();
      mutePost.setPost(post.get());
      mutePost.setUser(user.get());
      mutedPostRepository.save(mutePost);
      redirectAttributes.addFlashAttribute("MuteMessage", "post has been muted");

      return "redirect:/forum/post/all";
    }
    return "redirect:/forum/post/all";
  }

  // show the favourite post feed

  @GetMapping("/post/favfeed")
  public String favpostfeed(Model model, @AuthenticationPrincipal UserDetails userDetails)
      throws ResourceNotFoundException {
    Optional<User> user = userRepository.findByName(userDetails.getUsername());
    // favpostList = favPostRepository.findAllByUser(user.get());
    favMutePostService.favpostList = favMutePostService.findAllFavPostsByUser(user.get());
    model.addAttribute("favpostList", favMutePostService.favpostList);
    model.addAttribute("commenterName", userDetails.getUsername());
    return "forum/favPost";
  }

  // dummy all post feed (done by other group)

 

  // delete a post from favourite feed

  @PostMapping("post/favfeed/{postId}/delete")
  public String deleteFavPost(@PathVariable int postId, String commenterName) {
    Optional<User> user = userRepository.findByName(commenterName);
    Optional<Post> post = postRepository.findById(postId);

    if (user.isPresent() && post.isPresent()) {
      if (favPostRepository.existsByUserAndPost(user.get(), post.get())) {
        favMutePostService.removeFavPost(user.get(), post.get());
        return String.format("redirect:/forum/post/favfeed");
      }
    }
    return "redirect:/forum/post/error";
  }

  // delete post from mute feed

  @GetMapping("/post/mutefeed")

  public String mutedpostfeed(Model model, @AuthenticationPrincipal UserDetails userDetails)

      throws ResourceNotFoundException {

    Optional<User> user = userRepository.findByName(userDetails.getUsername());

    favMutePostService.mutedpostList = mutedPostRepository.findAllByUser(user.get());

    model.addAttribute("mutedpostList", favMutePostService.mutedpostList);

    model.addAttribute("commenterName", userDetails.getUsername());

    return "forum/mutePost";

  }

  @PostMapping("post/mutefeed/{postId}/delete")

  public String UnmutedPost(@PathVariable int postId, String commenterName) {

    Optional<User> user = userRepository.findByName(commenterName);

    Optional<Post> post = postRepository.findById(postId);

    if (user.isPresent() && post.isPresent()) {

      if (mutedPostRepository.existsByUserAndPost(user.get(), post.get())) {

        Mutedpost mutePost = mutedPostRepository.findByUserAndPost(user.get(), post.get());

        mutedPostRepository.delete(mutePost);

        return "redirect:/forum/post/mutefeed";

      }

    }

    return "redirect:/forum/post/error";

  }

}
