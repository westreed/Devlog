import { atom } from "recoil";

/* 0:light 1:dark 2:system */

const systemTheme = (theme) => {
  if (theme === 2) {
    if (
      window.matchMedia &&
      window.matchMedia("(prefers-color-scheme: dark)").matches
    ) {
      return 1;
    } else {
      return 0;
    }
  }
  return theme;
};

export const getThemeValue = () => {
  var theme = localStorage.getItem("theme");
  if (theme == null) {
    theme = 2; // system
  } else {
    theme = Number(theme);
  }
  return theme;
};

export const getTheme = () => {
  var theme = localStorage.getItem("theme");
  if (theme == null) {
    theme = 2; // system
    localStorage.setItem("theme", 2);
  } else {
    theme = Number(theme);
  }
  return systemTheme(theme);
};

export const setTheme = (flag) => {
  localStorage.setItem("theme", flag);
  return systemTheme(flag);
};

export const themeAtom = atom({
  key: "theme",
  default: getTheme(), // isDarkMode
});
