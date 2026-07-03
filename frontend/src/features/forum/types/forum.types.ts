export interface Pregunta {
  id: string;
  titulo: string;
  contenido: string;
  usuarioId: string;
  usuarioNombre: string;
  carreraId: string;
  carreraNombre: string;
  asignaturaId: string;
  asignaturaNombre: string;
  votos: number;
  votadoPorMi: boolean;
  totalRespuestas: number;
  createdAt: string;
  respuestas: Respuesta[] | null;
}

export interface Respuesta {
  id: string;
  preguntaId: string;
  usuarioId: string;
  usuarioNombre: string;
  contenido: string;
  votos: number;
  votadoPorMi: boolean;
  aceptada: boolean;
  createdAt: string;
}

export interface CreatePreguntaPayload {
  titulo: string;
  contenido: string;
  carreraId: string;
  asignaturaId: string;
}

export interface VotoResult {
  votos: number;
  votadoPorMi: boolean;
}
