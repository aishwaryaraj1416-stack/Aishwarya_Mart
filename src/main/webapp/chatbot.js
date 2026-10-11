(() => {
  const root = document.getElementById("boba-chat");
  if (!root) return;
  const launcher = root.querySelector("#boba-launcher");
  const panel = root.querySelector("#boba-panel");
  const close = root.querySelector("#boba-close");
  const form = root.querySelector("#boba-form");
  const input = root.querySelector("#boba-input");
  const messages = root.querySelector("#boba-messages");
  const send = root.querySelector("#boba-send");

  function openChat(open) {
    panel.hidden = !open;
    launcher.setAttribute("aria-expanded", String(open));
    if (open) input.focus();
  }
  function addMessage(text, type) {
    const bubble = document.createElement("div");
    bubble.className = "boba-message boba-" + type;
    bubble.textContent = text;
    messages.appendChild(bubble);
    messages.scrollTop = messages.scrollHeight;
    return bubble;
  }
  launcher.addEventListener("click", () => openChat(panel.hidden));
  close.addEventListener("click", () => openChat(false));

  root.querySelectorAll("[data-boba-prompt]").forEach(button => {
    button.addEventListener("click", () => {
      input.value = button.getAttribute("data-boba-prompt") || "";
      form.dispatchEvent(new Event("submit", { bubbles: true, cancelable: true }));
    });
  });

  form.addEventListener("submit", async event => {
    event.preventDefault();
    const message = input.value.trim();
    if (!message) return;
    addMessage(message, "user");
    input.value = "";
    input.disabled = true;
    send.disabled = true;
    const pending = addMessage("Thinking up something lovely… 🧋", "bot");
    try {
      const response = await fetch("api/v1/chat", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ message })
      });
      const result = await response.json();
      pending.remove();
      if (!response.ok || result.success !== true) {
        addMessage(result.error?.message || "Please try again in a moment.", "bot");
      } else {
        addMessage(result.data.reply, "bot");
      }
    } catch (error) {
      pending.remove();
      addMessage("I can't connect right now. Please try again in a moment. 💗", "bot");
      console.error("Boba chat error:", error);
    } finally {
      input.disabled = false;
      send.disabled = false;
      input.focus();
    }
  });
})();
