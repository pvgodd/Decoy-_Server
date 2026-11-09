(function () {
    "use strict";

    // 비밀번호 표시 토글
    document.querySelectorAll("[data-toggle=password]").forEach(function (btn) {
        btn.addEventListener("click", function () {
            var input = btn.closest(".pw-field").querySelector("input");
            if (!input) return;
            var show = input.type === "password";
            input.type = show ? "text" : "password";
            btn.textContent = show ? "숨김" : "표시";
        });
    });

    // 아이디 저장 (localStorage)
    var loginForm = document.querySelector("form[action$='/login']");
    var idInput = document.getElementById("loginId");
    var remember = document.getElementById("remember-id");
    if (loginForm && idInput && remember) {
        try {
            var saved = localStorage.getItem("portal.loginId");
            if (saved) { idInput.value = saved; remember.checked = true; }
        } catch (e) { /* ignore */ }
        loginForm.addEventListener("submit", function () {
            try {
                if (remember.checked) { localStorage.setItem("portal.loginId", idInput.value.trim()); }
                else { localStorage.removeItem("portal.loginId"); }
            } catch (e) { /* ignore */ }
        });
    }

    // 아이디 중복 확인
    var checkBtn = document.querySelector("[data-check-id]");
    if (checkBtn) {
        checkBtn.addEventListener("click", function () {
            var target = document.getElementById("loginId");
            var result = document.getElementById("loginId-check");
            var value = (target.value || "").trim();
            if (!/^[a-z0-9_]{4,20}$/.test(value)) {
                result.textContent = "영문 소문자·숫자·밑줄 4~20자로 입력하세요.";
                result.className = "hint field-error";
                return;
            }
            fetch(checkBtn.getAttribute("data-check-id") + "?loginId=" + encodeURIComponent(value))
                .then(function (r) { return r.json(); })
                .then(function (d) {
                    result.textContent = d.available ? "사용할 수 있는 아이디입니다." : "이미 사용 중인 아이디입니다.";
                    result.className = d.available ? "hint" : "hint field-error";
                    result.style.color = d.available ? "var(--success)" : "";
                })
                .catch(function () {
                    result.textContent = "확인 중 오류가 발생했습니다.";
                    result.className = "hint field-error";
                });
        });
    }

    // 삭제 확인
    document.querySelectorAll("form[data-confirm]").forEach(function (form) {
        form.addEventListener("submit", function (e) {
            if (!window.confirm(form.getAttribute("data-confirm"))) { e.preventDefault(); }
        });
    });
})();
