/* API adapter boundary. Set baseUrl and endpoint paths ONLY after receiving
   Controller/DTO contracts. No guessed endpoints or credentials are shipped. */
window.MfuApi = (() => {
  let configuration = null;
  return {
    configure({baseUrl, endpoints, getHeaders = () => ({})}) {
      configuration = {baseUrl, endpoints, getHeaders};
    },
    async request(operation, {method = 'GET', body, signal} = {}) {
      if (!configuration?.endpoints?.[operation]) {
        throw new Error('API operation is not configured: ' + operation);
      }
      const response = await fetch(new URL(configuration.endpoints[operation], configuration.baseUrl), {
        method, signal,
        headers: {...(body ? {'Content-Type':'application/json'} : {}), ...configuration.getHeaders()},
        ...(body ? {body:JSON.stringify(body)} : {})
      });
      if (!response.ok) throw new Error('API request failed (' + response.status + ')');
      return response.status === 204 ? null : response.json();
    }
  };
})();
