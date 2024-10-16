import React, { useEffect, useState } from "react";

import "./Posts.scss";
import { onErrorImg } from "utils/defaultImg";
import { get_posts_api } from "api/Posts";
import { useRecoilState, useRecoilValue } from "recoil";
import { categoryAtom } from "recoil/categoryAtom";
import { Dropdown, Pagination } from "flowbite-react";
import { paginationCustomTheme } from "styles/theme/pagination";
import { getDatetime } from "utils/getDatetime";
import { Link } from "react-router-dom";
import { authAtom } from "recoil/authAtom";

import nopostImg from "assets/nopost.png";

function PostCard(idx, post, setSelectCategory) {
  const createdAtFormat = getDatetime(post.createdAt);
  return (
    <div className="post" key={idx}>
      <Link to={`/post/${post.url}`}>
        <div className="post-item-top">
          {post.previewUrl !== null ? (
            <img
              className="post-item-img"
              src={post.previewUrl}
              alt="post"
              onError={onErrorImg}
            />
          ) : (
            <div className="post-item-img"></div>
          )}
        </div>
        <div className="post-item-bottom">
          <div className="post-item-content">
            {/* <button
            className="post-item-category"
            onClick={() => setSelectCategory(post.category.id)}
          >
            {post.category.name}
          </button> */}
            <div className="post-item-category">{post.category.name}</div>
            {/* <Link to={`/post/${post.url}`} className="post-item-title">
            {post.title}
          </Link> */}
            <div className="post-item-title">{post.title}</div>
          </div>
          <div className="post-item-chore">
            <div className="post-item-create">{createdAtFormat}</div>
            <div className="post-item-views">{`조회 ${post.views}`}</div>
          </div>
        </div>
        {/* <div className="author">by {post.author}</div> */}
        {/* <div className="content">{post.content}</div> */}
      </Link>
    </div>
  );
}

function Posts() {
  const authDto = useRecoilValue(authAtom);
  const [selectCategory, setSelectCategory] = useRecoilState(categoryAtom);
  const [posts, setPosts] = useState([]);
  const [page, setPage] = useState({
    totalPages: 0,
    currentPage: 0,
    totalElements: 0,
  });
  const [viewSize, setViewSize] = useState(10);

  useEffect(() => {
    PostEnvetHandler(selectCategory, page.currentPage, viewSize);
    // eslint-disable-next-line
  }, [selectCategory, authDto]);

  const PostEnvetHandler = async (category, curpage, view) => {
    await get_posts_api(category, curpage, view)
      .then((response) => {
        setPosts(response.data.posts);
        setPage({
          totalPages: response.data.totalPages,
          currentPage: response.data.currentPage,
          totalElements: response.data.totalElements,
        });
      })
      .catch((error) => {
        console.log(error);
      });
  };

  const onDropdownHandler = (e, value) => {
    // viewSize 변경시 무조건 첫번째 페이지로 이동
    setViewSize(value);
    setPage({
      ...page,
      currentPage: 0,
    });
    PostEnvetHandler(selectCategory, 0, value);
  };

  const onPaginationHandler = (next_page) => {
    setPage({
      ...page,
      currentPage: next_page - 1,
    });
    PostEnvetHandler(selectCategory, next_page - 1, viewSize);
  };

  if (page.totalElements === 0) {
    return (
      <div className="posts-container">
        <div className="posts-empty">
          <img src={nopostImg} alt="profile" onError={onErrorImg} />
          <p>글이 없네요.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="posts-container">
      <div className="posts-header">
        <h1 className="post-count">{page.totalElements} 포스트</h1>
        <div className="post-detail">
          <Dropdown label={`${viewSize}개 보기`} inline>
            <Dropdown.Item onClick={(e) => onDropdownHandler(e, 6)}>
              6개 보기
            </Dropdown.Item>
            <Dropdown.Item onClick={(e) => onDropdownHandler(e, 10)}>
              10개 보기
            </Dropdown.Item>
            <Dropdown.Item onClick={(e) => onDropdownHandler(e, 20)}>
              20개 보기
            </Dropdown.Item>
            <Dropdown.Item onClick={(e) => onDropdownHandler(e, 50)}>
              50개 보기
            </Dropdown.Item>
          </Dropdown>
        </div>
      </div>

      <div className="posts">
        {posts.map((val, idx) => PostCard(idx, val, setSelectCategory))}
      </div>
      <div className="flex overflow-x-auto sm:justify-center post-pageination">
        <Pagination
          theme={paginationCustomTheme}
          layout="pagination"
          currentPage={page.currentPage + 1}
          totalPages={page.totalPages}
          onPageChange={onPaginationHandler}
          previousLabel="이전"
          nextLabel="다음"
          showIcons
        />
      </div>
    </div>
  );
}

export default Posts;
