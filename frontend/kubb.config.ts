import { defineConfig } from '@kubb/core';
import { pluginOas } from '@kubb/plugin-oas';
import { pluginTs } from '@kubb/plugin-ts';
import { pluginReactQuery } from '@kubb/plugin-react-query';

export default defineConfig({
  input: {
    path: '../api-spec/openapi.yaml',
  },
  output: {
    path: './src/gen',
    clean: true,
  },
  plugins: [
    pluginOas(),
    pluginTs(),
    pluginReactQuery({
      client: {
        importPath: '../../client.ts',
      },
      query: {
        methods: ['get'],
        importPath: '@tanstack/react-query',
      },
      mutation: {
        methods: ['post', 'put', 'patch', 'delete'],
        importPath: '@tanstack/react-query',
      },
    }),
  ],
});
