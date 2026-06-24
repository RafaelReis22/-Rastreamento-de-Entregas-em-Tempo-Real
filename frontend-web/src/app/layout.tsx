import type { Metadata } from 'next';

export const metadata: Metadata = {
  title: 'Rastreamento de Entregas',
  description: 'Sistema de rastreamento de entregas em tempo real',
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="pt-BR">
      <body>{children}</body>
    </html>
  );
}
