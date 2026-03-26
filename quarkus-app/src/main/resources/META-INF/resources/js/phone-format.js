(function (root, factory) {
  const api = factory();

  root.AgendaPhoneFormatter = api;

  if (typeof module === 'object' && module.exports) {
    module.exports = api;
  }
})(typeof globalThis !== 'undefined' ? globalThis : this, function () {
  function formatPhoneNumberForDisplay(value) {
    const originalValue = String(value ?? '').trim();
    const digits = originalValue.replace(/\D/g, '');

    if (digits.length === 11) {
      return `(${digits.slice(0, 2)}) ${digits.slice(2, 7)}-${digits.slice(7)}`;
    }

    if (digits.length === 10) {
      return `(${digits.slice(0, 2)}) ${digits.slice(2, 6)}-${digits.slice(6)}`;
    }

    return originalValue;
  }

  return {
    formatPhoneNumberForDisplay,
  };
});