import {get_post_files_api, get_post_url_api} from "@/api/Posts";
import Comment from "@/components/base/comment";
import Post from "@/components/post";
import React, {useEffect, useState} from "react";
import {NavigationType, useLocation, useNavigate, useNavigationType, useParams} from "react-router-dom";
import {useRecoilState, useRecoilValue} from "recoil";
import {authAtom} from "@/recoil/authAtom";
import {commentAtom, commentsAtom, CommentsData, CommentState,} from "@/recoil/commentAtom";
import {ga4Atom} from "@/recoil/ga4Atom";
import {postAtom} from "@/recoil/postAtom";
import {sendPageView} from "@/utils/reactGA4";
import {sortComments} from "@/utils/sortComments";


const resetPostData = async(setPostContent, setComments) => {
  await setPostContent("");
  await setComments(new CommentsData());
}

function PostCommentContainer({ ...props }) {
  const navigate = useNavigate();
  const navigateType = useNavigationType();
  const location = useLocation();
  const { postUrl } = useParams();
  const [authDto] = useRecoilState(authAtom);
  const initialized = useRecoilValue(ga4Atom);
  const [, setPostContent] = useRecoilState(postAtom);
  const [, setCommentState] = useRecoilState(commentAtom);
  const [, setComments] = useRecoilState(commentsAtom);
  const [likes, setLikes] = useState();

  useEffect(() => {
    const getPost = async () => {
      await resetPostData(setPostContent, setComments);
      await get_post_url_api(postUrl).then(async (res) => {
          const postData = res.data?.post;
          sendPageView(location.pathname, postData.title, initialized);
          try {
            const result = await get_post_files_api(postData.id);
            postData["files"] = result.data || [];
          } catch (error) {
            console.error("Failed to get post files:", error);
            throw error;
          }
          setPostContent(postData);
          const commentsObj = new CommentsData(
            sortComments(res.data?.comments),
            res.data?.comments.length
          );
          setComments(commentsObj);
          setCommentState(new CommentState(res.data?.commentFlag));
          setLikes(res.data?.likes);
        }).catch((error) => {
          console.error("Failed to get post:", error);
          if (navigateType === NavigationType.Pop) {
            navigate(-1);
          } else {
            navigate("/");
          }
        });
    };
    getPost();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [postUrl, authDto]);

  return (
    <>
      <Post {...props} likes={likes} setLikes={setLikes} />
      <Comment {...props} />
    </>
  );
}

export default PostCommentContainer;
