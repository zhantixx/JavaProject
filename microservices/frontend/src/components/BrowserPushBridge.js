import { useEffect, useRef } from 'react';
import { notifications, profile } from '../services/api';

const STORAGE_KEY = 'shownBrowserNotifications';
const MAX_STORED_IDS = 100;

function loadShownIds() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? parsed : [];
  } catch (error) {
    return [];
  }
}

function saveShownIds(ids) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(ids.slice(-MAX_STORED_IDS)));
}

function canUseBrowserNotifications() {
  return typeof window !== 'undefined' && 'Notification' in window;
}

export default function BrowserPushBridge() {
  const startedRef = useRef(false);

  useEffect(() => {
    if (startedRef.current) {
      return undefined;
    }
    startedRef.current = true;

    if (!canUseBrowserNotifications()) {
      return undefined;
    }

    let cancelled = false;
    let intervalId = null;
    let currentUserId = null;
    let shownIds = loadShownIds();

    const showBrowserNotification = (item) => {
      if (Notification.permission !== 'granted') {
        return;
      }

      const popup = new Notification(item.title || 'SanaBot', {
        body: item.content || 'Жаңа еске салғыш келді.',
        tag: `sanabot-${item.id}`,
      });

      popup.onclick = () => {
        window.focus();
        window.location.href = '/notifications';
      };
    };

    const syncNotifications = async () => {
      if (cancelled || Notification.permission !== 'granted' || !currentUserId) {
        return;
      }

      try {
        const listResponse = await notifications.list(currentUserId);
        const items = Array.isArray(listResponse.data) ? listResponse.data : [];
        const freshPushItems = items
          .filter((item) => item.type === 'PUSH')
          .filter((item) => !item.readAt)
          .filter((item) => !shownIds.includes(item.id))
          .slice(0, 3);

        freshPushItems.forEach((item) => {
          showBrowserNotification(item);
          shownIds.push(item.id);
        });

        if (freshPushItems.length) {
          shownIds = shownIds.slice(-MAX_STORED_IDS);
          saveShownIds(shownIds);
        }
      } catch (error) {
        // Silent polling failure keeps the shell stable.
      }
    };

    const start = async () => {
      try {
        const meResponse = await profile.me();
        currentUserId = meResponse.data?.id || null;
        if (!currentUserId) {
          return;
        }

        if (Notification.permission === 'default') {
          await Notification.requestPermission();
        }

        if (Notification.permission !== 'granted') {
          return;
        }

        await syncNotifications();
        intervalId = window.setInterval(syncNotifications, 30000);
      } catch (error) {
        // Session may still be settling; skip quietly.
      }
    };

    start();

    return () => {
      cancelled = true;
      if (intervalId) {
        window.clearInterval(intervalId);
      }
    };
  }, []);

  return null;
}
