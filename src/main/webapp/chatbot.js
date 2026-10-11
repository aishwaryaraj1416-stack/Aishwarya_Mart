(() => {
  "use strict";
  const mount = document.getElementById("boba-chat");
  if (!mount) return;

  mount.innerHTML = `
    <button class="boba-launcher" type="button" aria-label="Open Boba shopping assistant" aria-expanded="false">
      <span class="boba-cup" aria-hidden="true">🧋</span>
      <span class="boba-launcher-label">Ask Boba</span>
    </button>
    <section class="boba-panel" role="dialog" aria-label="Boba AI shopping assistant" aria-modal="false" hidden>
      <header class="boba-header">
        <div class="boba-avatar" aria-hidden="true">🧋</div>
        <div class="boba-heading"><strong>Boba</strong><span>Your Aishwarya Mart buddy</span></div>
        <button class="boba-close" type="button" aria-label="Close chat">×</button>
      </header>
      <div class="boba-messages" aria-live="polite" aria-relevant="additions">
        <div class="boba-message boba-bot">Hiii! I'm Boba 🧋 I can help you find products, shop within a budget, and manage your bag. What can I help you with?</div>
      </div>
      <div class="boba-suggestions">
        <button type="button" data-message="Show products">Browse products</button>
        <button type="button" data-message="Show products under 1000">Under ₹1000</button>
        <button type="button" data-message="Show my cart">My bag</button>
      </div>
      <form class="boba-form">
        <label class="boba-sr-only" for="boba-input">Message Boba</label>
        <input id="boba-input" name="message" maxlength="500" autocomplete="off" placeholder="Ask Boba anything…" required>
        <button class="boba-send" type="submit" aria-label="Send message">➤</button>
      </form>
      <p class="boba-footnote">AI can make mistakes. Check product details before ordering.</p>
    </section>`;

  const launcher = mount.querySelector(".boba-launcher");
  const panel = mount.querySelector(".boba-panel");
  const close = mount.querySelector(".boba-close");
  const form = mount.querySelector(".boba-form");
  const input = mount.querySelector("#boba-input");
  const messages = mount.querySelector(".boba-messages");
  const send = mount.querySelector(".boba-send");
  let sending = false;

  function openChat() {
    panel.hidden = false;
    launcher.setAttribute("aria-expanded", "true");
    launcher.classList.add("boba-launcher-open");
    input.focus();
  }
  function closeChat() {
    panel.hidden = true;
    launcher.setAttribute("aria-expanded", "false");
    launcher.classList.remove("boba-launcher-open");
  }
  function addMessage(text, who) {
    const bubble = document.createElement("div");
    bubble.className = "boba-message " + (who === "user" ? "boba-user" : "boba-bot");
    bubble.textContent = text;
    messages.appendChild(bubble);
    messages.scrollTop = messages.scrollHeight;
    return bubble;
  }
  async function sendMessage(text) {
    const message = String(text || "").trim();
    if (!message || sending) return;
    openChat();
    addMessage(message, "user");
    input.value = "";
    sending = true;
    send.disabled = true;
    const waiting = addMessage("Boba is thinking… 🧋", "boba");
    waiting.classList.add("boba-thinking");
    try {
      const response = await fetch("api/v1/chat", {
        method: "POST",
        headers: { "Content-Type": "application/json", "Accept": "application/json" },
        credentials: "same-origin",
        body: JSON.stringify({ message })
      });
      const result = await response.json();
      waiting.remove();
      if (!response.ok || !result.success) {
        const errorText = result && result.error && result.error.message;
        addMessage(errorText || "Oops, I couldn't send that. Please try again.", "bot");
      } else {
        addMessage(result.data && result.data.reply ? result.data.reply : "I'm not sure what to say just yet. Try asking another way!", "bot");
      }
    } catch (error) {
      waiting.remove();
      addMessage("I couldn't connect just now. Check that Aishwarya Mart is running, then try again 💗", "bot");
    } finally {
      sending = false;
      send.disabled = false;
      input.focus();
    }
  }

  launcher.addEventListener("click", () => panel.hidden ? openChat() : closeChat());
  close.addEventListener("click", closeChat);
  form.addEventListener("submit", event => {
    event.preventDefault();
    sendMessage(input.value);
  });
  mount.querySelectorAll("[data-message]").forEach(button => {
    button.addEventListener("click", () => sendMessage(button.getAttribute("data-message")));
  });
  document.addEventListener("keydown", event => {
    if (event.key === "Escape" && !panel.hidden) closeChat();
  });
})();