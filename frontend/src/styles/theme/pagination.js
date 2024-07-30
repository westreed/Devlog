export const paginationCustomTheme = {
  base: "",
  layout: {
    table: {
      base: "text-sm text-gray-700 dark:text-gray-400",
      span: "font-semibold text-gray-900 dark:text-white",
    },
  },
  pages: {
    base: "xs:mt-0 mt-2 inline-flex items-center -space-x-px",
    showIcon: "inline-flex",
    previous: {
      base: "ml-0 rounded-l-lg border border-gray-300 bg-white px-3 py-2 leading-tight text-gray-500 enabled:hover:bg-gray-100 enabled:hover:text-gray-700 dark:border-zinc-800 dark:bg-[#1c1c1c] dark:text-gray-400 enabled:dark:hover:bg-[#242424] enabled:dark:hover:text-white",
      icon: "h-5 w-5",
    },
    next: {
      base: "rounded-r-lg border border-gray-300 bg-white px-3 py-2 leading-tight text-gray-500 enabled:hover:bg-gray-100 enabled:hover:text-gray-700 dark:border-zinc-800 dark:bg-[#1c1c1c] dark:text-gray-400 enabled:dark:hover:bg-[#242424] enabled:dark:hover:text-white",
      icon: "h-5 w-5",
    },
    selector: {
      base: "w-12 border border-gray-300 bg-white py-2 leading-tight text-gray-500 enabled:hover:bg-gray-100 enabled:hover:text-gray-700 dark:border-zinc-800 dark:bg-[#1c1c1c] dark:text-gray-400 enabled:dark:hover:bg-[#242424] enabled:dark:hover:text-white",
      active:
        "bg-cyan-50 text-cyan-600 hover:bg-cyan-100 hover:text-cyan-700 dark:border-zinc-800 dark:bg-[#242424] dark:text-white",
      disabled: "cursor-not-allowed opacity-50",
    },
  },
};
