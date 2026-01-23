export type RequestConfig<TData = unknown> = {
  url?: string;
  method: 'GET' | 'PUT' | 'PATCH' | 'POST' | 'DELETE';
  params?: object;
  data?: TData | FormData;
  responseType?: 'arraybuffer' | 'blob' | 'document' | 'json' | 'text' | 'stream';
  signal?: AbortSignal;
  headers?: HeadersInit;
};

export type ResponseConfig<TData = unknown> = {
  data: TData;
  status: number;
  statusText: string;
};

export type ResponseErrorConfig<TError = unknown> = ResponseConfig<TError>;

const client = async <TData, _TError = unknown, TVariables = unknown>(
  config: RequestConfig<TVariables>
): Promise<ResponseConfig<TData>> => {
  const { url, method, data, signal, headers } = config;

  const response = await fetch(url ?? '', {
    method: method.toUpperCase(),
    body: data ? JSON.stringify(data) : undefined,
    signal,
    headers: {
      'Content-Type': 'application/json',
      ...headers,
    },
  });

  const responseData = await response.json();

  if (!response.ok) {
    throw {
      data: responseData,
      status: response.status,
      statusText: response.statusText,
    };
  }

  return {
    data: responseData,
    status: response.status,
    statusText: response.statusText,
  };
};

export default client;
